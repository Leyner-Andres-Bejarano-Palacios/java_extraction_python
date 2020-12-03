package com.mycompany.app;
 

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		Date time = new Date();
		
		PipelineOptions options = PipelineOptionsFactory.create();
		Pipeline p = Pipeline.create(options);
		

		PCollection<String> data = p.apply(JdbcIO.<String>read()
				.withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
						.create("com.mysql.jdbc.Driver","jdbc:mysql://db:3306/information_schema")	
						.withUsername("wordpress").withPassword("wordpress"))
			
				.withQuery("select * from CHARACTER_SETS WHERE CHARACTER_SET_NAME != ?")
				.withCoder(StringUtf8Coder.of())
				.withStatementPreparator(new JdbcIO.StatementPreparator() {
					private static final long serialVersionUID = 1L;
					@Override
					public void setParameters(PreparedStatement preparedStatement) throws Exception {
						preparedStatement.setString(1, "NVDA");
						
					}
				})
				.withRowMapper(new JdbcIO.RowMapper<String>() {
					private static final long serialVersionUID = 1L;
					public String mapRow(ResultSet resultSet) throws Exception {
						return "col 1: "+resultSet.getString(1)+"\ncol 2: "+resultSet.getString(2)+
								"\ncol 3: "+resultSet.getString(3);
					}
				}));
		
		@SuppressWarnings("unused")
		PDone output = data.apply(ParDo.of(new DoFn<String, String>(){
			private static final long serialVersionUID = 1L;
			@ProcessElement
			public void processElement(ProcessContext ctx) {
				System.out.println(ctx.element());
				ctx.output(ctx.element());
			}
		})).apply(TextIO.write().to("output-in:"+time+".txt"));

		State result = p.run().waitUntilFinish();	
		System.out.println(result); 
    }
}
