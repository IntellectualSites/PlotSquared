package com.github.intellectualsites.plotsquared.plot.object.stream;

import java.io.IOException;
import java.io.OutputStream;

public class AbstractDelegateOutputStream extends OutputStream {

  private final OutputStream parent;

  public AbstractDelegateOutputStream(OutputStream os) {
    this.parent = os;
  }

  @Override
  public void write(int b) throws IOException {
    parent.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    parent.write(b);
  }
    
  @Override
  public void flush() throws IOException {
    parent.flush();
  }

  @Override
  public void close() throws IOException {
    parent.close();
  }
}

