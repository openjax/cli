package org.apache.commons.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FixedHelpFormatter extends HelpFormatter {
  @Override
  @SuppressWarnings("unchecked")
  protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
    final String lpad = createPadding(leftPad);
    final String dpad = createPadding(descPad);

    // first create list containing only <lpad>-a,--aaa where
    // -a is opt and --aaa is long opt; in parallel look for
    // the longest opt string this list will be then used to
    // sort options ascending
    final List<StringBuffer> prefixList = new ArrayList<StringBuffer>();
    final List<Option> optList = options.helpOptions();
    Collections.sort(optList, getOptionComparator());

    int max = 0;
    StringBuffer optBuf;
    for (final Iterator<Option> i = optList.iterator(); i.hasNext();) {
      final Option option = i.next();
      optBuf = new StringBuffer(8);

      if (option.getOpt() == null) {
        optBuf.append(lpad).append("   " + getLongOptPrefix()).append(option.getLongOpt());
      }
      else {
        optBuf.append(lpad).append(getOptPrefix()).append(option.getOpt());

        if (option.hasLongOpt()) {
          optBuf.append(',').append(getLongOptPrefix()).append(option.getLongOpt());
        }
      }

      if (option.hasArg()) {
        if (option.hasArgName()) {
          if (option.isRequired())
            optBuf.append(" <").append(option.getArgName()).append(">");
          else
            optBuf.append(" [").append(option.getArgName()).append("]");
        }
        else {
          optBuf.append(' ');
        }
      }

      prefixList.add(optBuf);
      max = optBuf.length() > max ? optBuf.length() : max;
    }

    int x = 0;
    for (final Iterator<Option> i = optList.iterator(); i.hasNext();) {
      final Option option = i.next();
      optBuf = new StringBuffer(prefixList.get(x++).toString());

      if (optBuf.length() < max) {
        optBuf.append(createPadding(max - optBuf.length()));
      }

      optBuf.append(dpad);

      final int nextLineTabStop = max + descPad;
      if (option.getDescription() != null) {
        optBuf.append(option.getDescription());
      }

      renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());
      if (i.hasNext()) {
        sb.append(getNewLine());
      }
    }

    return sb;
  }
}