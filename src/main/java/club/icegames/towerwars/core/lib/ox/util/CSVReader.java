package club.icegames.towerwars.core.lib.ox.util;

import static com.google.common.base.Preconditions.checkState;
import static club.icegames.towerwars.core.lib.ox.util.Utils.propagate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CSVReader {

  private final BufferedReader br;

  private StringBuilder sb = new StringBuilder();
  private int lastSize = 0;
  private char delimiter = ',';
  private char escape = '"';
  private boolean reuseBuffer = false;
  private List<String> buffer;

  public CSVReader(InputStream is) {
    this(new InputStreamReader(is, StandardCharsets.UTF_8));
  }

  public CSVReader(InputStream is, Charset charset) {
    this(new InputStreamReader(is, charset));
  }

  public CSVReader(String s) {
    this(new StringReader(s));
  }

  public CSVReader(Reader reader) {
    br = new BufferedReader(reader);
  }

  public CSVReader reuseBuffer() {
    reuseBuffer = true;
    return this;
  }

  public CSVReader delimiter(char delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  public CSVReader escape(char escapeCharacter) {
    this.escape = escapeCharacter;
    return this;
  }

  public CSVReader skipLines(int nLines) {
    for (int i = 0; i < nLines; i++) {
      try {
        br.readLine();
      } catch (IOException e) {
        throw propagate(e);
      }
    }
    return this;
  }

  public Map<String, Integer> getHeaderIndex() {
    Map<String, Integer> ret = Maps.newLinkedHashMap();
    List<String> row = nextLine();
    for (int i = 0; i < row.size(); i++) {
      ret.put(row.get(i), i);
    }
    return ret;
  }

  public void forEach(Consumer<List<String>> callback) {
    List<String> m = nextLine();
    while (m != null) {
      callback.accept(m);
      m = nextLine();
    }
  }

  public List<List<String>> getLines() {
    List<List<String>> ret = Lists.newArrayList();
    forEach(ret::add);
    return ret;
  }

  public List<String> nextLine() {
    String line;
    try {
      line = br.readLine();
    } catch (IOException e) {
      throw propagate(e);
    }
    if (line == null) {
      return null;
    }

    try {
      return parseLine(line, br);
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  private List<String> parseLine(String line, BufferedReader br) throws Exception {
    List<String> ret = buffer;
    if (ret == null) {
      ret = Lists.newArrayListWithCapacity(lastSize);
      if (reuseBuffer) {
        buffer = ret;
      }
    } else {
      buffer.clear();
    }

    boolean escaped = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == escape) {
        if (escaped) {
          // next character must be a delimiter in order to unescape (or we've reached end of file)
          if (i == line.length() - 1 || line.charAt(i + 1) == delimiter) {
            escaped = false;
          }
        } else {
          escaped = true;
        }
      } else if (!escaped && c == delimiter) {
        ret.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
      if (i == line.length() - 1) {
        if (escaped) {
          // there was a newline which was escaped!
          line = line + '\n' + br.readLine();
        }
      }
    }
    ret.add(sb.toString());
    sb.setLength(0);

    if (lastSize == 0) {
      lastSize = ret.size();
    } else {
      checkState(ret.size() == lastSize, "Found a row with " + ret.size() +
          " elements when we previously saw a row with " + lastSize + " elements.");
    }
    return ret;
  }

  public static CSVReader from(InputStream is) {
    return new CSVReader(is);
  }

}
