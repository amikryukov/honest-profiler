package com.insightfullogic.honest_profiler.core.aggregation;

import static com.insightfullogic.honest_profiler.core.collector.lean.LogEventFactory.methodFor;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.insightfullogic.honest_profiler.core.aggregation.grouping.FrameGrouping;
import com.insightfullogic.honest_profiler.core.aggregation.grouping.ThreadGrouping;
import com.insightfullogic.honest_profiler.core.parser.Method;
import com.insightfullogic.honest_profiler.core.parser.StackFrame;
import com.insightfullogic.honest_profiler.core.parser.ThreadMeta;

public class AggregationUtil
{
    // Time conversion

    public static final long nano(int seconds)
    {
        return SECONDS.toNanos(seconds);
    }

    // Method / Frame key conversion

    // -> Single Frame
    public static final String keyFor(FrameGrouping frameGrouping, StackFrame frame)
    {
        switch (frameGrouping)
        {
            case BY_FQMN:
                return toFqmn(methodFor(frame));
            case BY_FQMN_LINENR:
                StringBuilder result = new StringBuilder(toFqmn(methodFor(frame)));
                result.append(":").append(frame.getLineNumber());
                return result.toString();
            case BY_BCI:
                result = new StringBuilder(toFqmn(methodFor(frame)));
                result.append(":").append(frame.getBci());
                return result.toString();
            case BY_METHOD_ID:
                result = new StringBuilder();
                result.append("(").append(methodFor(frame).getMethodId()).append(") ");
                result.append(toFqmn(methodFor(frame)));
                return result.toString();
        }

        return null;
    }

    // -> List of parents identifying a Tree node
    public static final String[] keysFor(ThreadGrouping threadGrouping, FrameGrouping frameGrouping,
        ThreadMeta thread, StackFrame... frames)
    {

        String[] result = new String[frames.length + 1];

        switch (threadGrouping)
        {
            case BY_ID:
                result[0] = getThreadIdKey(thread);
                break;
            case BY_NAME:
                result[0] = getThreadNameKey(thread);
                break;
            case ALL_TOGETHER:
                result[0] = "All Threads";
                break;
        }

        int idx = 1;
        for (StackFrame frame : frames)
        {
            result[idx++] = keyFor(frameGrouping, frame);
        }

        return result;
    }
    private static final String toFqmn(Method method)
    {
        StringBuilder result = new StringBuilder();
        result.append(method.getClassName()).append(".");
        result.append(method.getMethodName());
        return result.toString();
    }

    // Thread key conversion

    public static final String getThreadIdKey(ThreadMeta thread)
    {
        if (thread == null)
        {
            return "Unknown Thread <Unknown ID>";
        }

        StringBuilder result = new StringBuilder();
        result.append(getThreadName(thread));
        result.append(" <").append(thread.getThreadId()).append(">");
        return result.toString();
    }

    public static final String getThreadNameKey(ThreadMeta thread)
    {
        if (thread == null || thread.getThreadName() == null || thread.getThreadName().isEmpty())
        {
            return "Unknown Thread(s)";
        }

        return thread.getThreadName();
    }

    private static final String getThreadName(ThreadMeta thread)
    {
        if (thread == null || thread.getThreadName() == null || thread.getThreadName().isEmpty())
        {
            return "Unknown";
        }

        return thread.getThreadName();
    }
}
