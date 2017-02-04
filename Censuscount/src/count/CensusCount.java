package count;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CensusCount {
	// mapper
	public static class WordMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private Text data = new Text();

		String gender;
		String statenew;

		@Override
		public void map(LongWritable key, Text value, Context c) throws IOException, InterruptedException {
			String str = value.toString();
			String[] strList = str.split(",");
			String state = strList[5];
			if (state.equals("06")) {
				statenew = "California ";
			} else if (state.equals("08")) {
				statenew = "Colorado   ";
			} else if (state.equals("53")) {
				statenew = "Washington  ";
			} else if (state.equals("48")) {
				statenew = "Texas       ";
			} else if (state.equals("42")) {
				statenew = "Pennsylvania";
			} else if (state.equals("50")) {
				statenew = "Vermont     ";
			} else if (state.equals("12")) {
				statenew = "Florida     ";
			} else {
				statenew = "others";
			}

			String serialno = strList[0];
			String year = serialno.substring(0, 4);
			String sex = strList[69];
			if (sex.equals("1")) {
				gender = "M";
			} else {
				gender = "F";
			}
			String w = strList[7];
			if (!w.equals("PWGTP") && !statenew.equals("others")) {
				int weight = Integer.parseInt(w);
				data.set(year + "," + gender + "," + statenew);

				c.write(data, new IntWritable(weight));
			}

		}
	}

	// reducer
	public static class WordReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context c) throws IOException, InterruptedException {

			double tot = 0;
			int count = 0;
			for (IntWritable weight : values) {
				tot += weight.get();
				count++;
			}
			tot = tot / count;
			c.write(key, new DoubleWritable(tot));

		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job j2 = new Job(conf);
		j2.setJobName("Censuscount job");
		j2.setJarByClass(CensusCount.class);
		// Mapper input and output
		j2.setMapOutputKeyClass(Text.class);
		j2.setMapOutputValueClass(IntWritable.class);
		// Reducer input and output
		j2.setOutputKeyClass(Text.class);
		// j2.setOutputValueClass(Text.class);
		j2.setOutputValueClass(DoubleWritable.class);
		// file input and output of the whole program
		j2.setInputFormatClass(TextInputFormat.class);
		j2.setOutputFormatClass(TextOutputFormat.class);
		// Set the mapper class
		j2.setMapperClass(WordMapper.class);
		// set the combiner class for custom combiner
		// j2.setCombinerClass(WordReducer.class);
		// Set the reducer class
		j2.setReducerClass(WordReducer.class);
		// set the number of reducer if it is zero means there is no reducer
		// j2.setNumReduceTasks(0);
		FileOutputFormat.setOutputPath(j2, new Path(args[1]));
		FileInputFormat.addInputPath(j2, new Path(args[0]));
		j2.waitForCompletion(true);
	}
}