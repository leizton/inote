package com.bytedance.tsd.storage.mstore;

import com.google.common.collect.Lists;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 2018/5/17
 */
@SuppressWarnings({"WeakerAccess", "RedundantThrows", "unused"})
public class MultiFileInputFormat extends FileInputFormat<LongWritable, Text> {
  private static final Logger LOG = LoggerFactory.getLogger(MultiFileInputFormat.class);
  private static final int BATCH_NUM = 5;

  @Override
  public List<InputSplit> getSplits(JobContext job) throws IOException {
    List<InputSplit> splits = super.getSplits(job);
    List<InputSplit> outSplits = new ArrayList<>(splits.size() / BATCH_NUM + 1);
    for (List<InputSplit> splitList : Lists.partition(splits, BATCH_NUM)) {
      if (!splitList.isEmpty()) {
        outSplits.add(new MultiInputSplit(splitList));
      }
    }
    LOG.info("MultiFileInputFormat originNum={} combineNum={}", splits.size(), outSplits.size());
    return outSplits;
  }

  @Override
  public RecordReader<LongWritable, Text> createRecordReader(
      InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
    if (!(split instanceof MultiInputSplit)) {
      throw new IOException("split not MultiInputSplit");
    }
    return new MultiFileLineRecordReader((MultiInputSplit) split);
  }

  public static final class MultiInputSplit extends InputSplit implements Writable {

    private List<FileSplit> splits;

    public MultiInputSplit() {
    }

    public MultiInputSplit(List<InputSplit> splits) {
      this.splits = new ArrayList<>(splits.size());
      for (InputSplit input : splits) {
        this.splits.add((FileSplit) input);
      }
    }

    @Override
    public long getLength() throws IOException, InterruptedException {
      long lengthSum = 0;
      for (FileSplit split : splits) {
        lengthSum += split.getLength();
      }
      return lengthSum;
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
      return splits.stream().map(in -> in.getPath().getName()).toArray(String[]::new);
    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(splits.size());
      for (FileSplit input : splits) {
        input.write(out);
      }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      int inputNum = in.readInt();
      splits = new ArrayList<>(inputNum);
      for (int i = 0; i < inputNum; i++) {
        FileSplit split = new FileSplit();
        split.readFields(in);
        splits.add(split);
      }
    }
  }

  public static final class MultiFileLineRecordReader extends RecordReader<LongWritable, Text> {

    private final MultiInputSplit input;
    private List<LineRecordReader> readers;
    private int currReaderPos;
    private LineRecordReader currReader;

    public MultiFileLineRecordReader(MultiInputSplit input) {
      this.input = input;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
      readers = new ArrayList<>(input.splits.size());
      for (FileSplit split1 : input.splits) {
        LineRecordReader reader = new LineRecordReader();
        reader.initialize(split1, context);
        readers.add(reader);
      }
      currReaderPos = 0;
      currReader = readers.isEmpty() ? null : readers.get(currReaderPos);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
      if (currReader == null) {
        return false;
      }
      if (currReader.nextKeyValue()) {
        return true;
      }
      if (currReaderPos < readers.size() - 1) {
        currReader.close();
        readers.set(currReaderPos, null);
        currReader = readers.get(++currReaderPos);
        return currReader.nextKeyValue();
      }
      currReaderPos++;
      currReader = null;
      return false;
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
      return currReader.getCurrentKey();
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
      return currReader.getCurrentValue();
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
      if (currReader != null) {
        return (currReaderPos + currReader.getProgress()) / BATCH_NUM;
      }
      return 1;
    }

    @Override
    public void close() throws IOException {
      for (LineRecordReader reader : readers) {
        if (reader != null) {
          reader.close();
        }
      }
    }
  }
}
