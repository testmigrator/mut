import com.test.migration.entity.po.ApiBasic;
import com.test.migration.entity.po.ApiMapping;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import utils.CallUtil;
import utils.JsonUtil;
import utils.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiNum {
    @Test
    public void test1(){
        StringUtils.replace("", null, null, 2);
    }

    private static final String[] ARRAY_LIST = {"foo", "bar", "baz"};
    private static final String[] EMPTY_ARRAY_LIST = {};
    private static final String[] NULL_ARRAY_LIST = {null};
    private static final Object[] NULL_TO_STRING_LIST = {
            new Object() {
                @Override
                public String toString() {
                    return null;
                }
            }
    };
    private static final String[] MIXED_ARRAY_LIST = {null, "", "foo"};
    private static final Object[] MIXED_TYPE_LIST = {"foo", Long.valueOf(2L)};
    private static final long[] LONG_PRIM_LIST = {1, 2};
    private static final int[] INT_PRIM_LIST = {1, 2};
    private static final byte[] BYTE_PRIM_LIST = {1, 2};
    private static final short[] SHORT_PRIM_LIST = {1, 2};
    private static final char[] CHAR_PRIM_LIST = {'1', '2'};
    private static final float[] FLOAT_PRIM_LIST = {1, 2};
    private static final double[] DOUBLE_PRIM_LIST = {1, 2};
    private static final List<String> MIXED_STRING_LIST = Arrays.asList(null, "", "foo");
    private static final List<Object> MIXED_TYPE_OBJECT_LIST = Arrays.<Object>asList("foo", Long.valueOf(2L));
    private static final List<String> STRING_LIST = Arrays.asList("foo", "bar", "baz");
    private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();
    private static final List<String> NULL_STRING_LIST = Collections.singletonList(null);

    private static final String SEPARATOR = ",";
    private static final char SEPARATOR_CHAR = ';';
    private static final char COMMA_SEPARATOR_CHAR = ',';

    private static final String TEXT_LIST = "foo,bar,baz";
    private static final String TEXT_LIST_CHAR = "foo;bar;baz";
    private static final String TEXT_LIST_NOSEP = "foobarbaz";

    private static final String FOO_UNCAP = "foo";
    private static final String FOO_CAP = "Foo";

    private static final String SENTENCE_UNCAP = "foo bar baz";
    private static final String SENTENCE_CAP = "Foo Bar Baz";

    private static final boolean[] EMPTY = {};
    private static final boolean[] ARRAY_FALSE_FALSE = {false, false};
    private static final boolean[] ARRAY_FALSE_TRUE = {false, true};
    private static final boolean[] ARRAY_FALSE_TRUE_FALSE = {false, true, false};

    @Test
    public void testJoin_ArrayCharSeparator() {

        assertNull(StringUtils.join((Object[]) null, ','));
        assertEquals(TEXT_LIST_CHAR, StringUtils.join(ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals(";;foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("foo;2", StringUtils.join(MIXED_TYPE_LIST, SEPARATOR_CHAR));

        assertNull(StringUtils.join((Object[]) null, ',', 0, 1));
        assertEquals("/", StringUtils.join(MIXED_ARRAY_LIST, '/', 0, MIXED_ARRAY_LIST.length - 1));
        assertEquals("foo", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 1));
        assertEquals("null", StringUtils.join(NULL_TO_STRING_LIST, '/', 0, 1));
        assertEquals("foo/2", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 2));
        assertEquals("2", StringUtils.join(MIXED_TYPE_LIST, '/', 1, 2));
        assertEquals("", StringUtils.join(MIXED_TYPE_LIST, '/', 2, 1));
    }

    @Test
    public void testJoin_ArrayCharSeparator_test() {

        assertEquals(";;foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR_CHAR));

    }


    // These are ints not enumerations as they represent genuine int values
    /**
     * Constant (1) representing January, the first month (ISO)
     */
    public static final int JANUARY = 1;

    /**
     * Constant (2) representing February, the second month (ISO)
     */
    public static final int FEBRUARY = 2;

    /**
     * Constant (3) representing March, the third month (ISO)
     */
    public static final int MARCH = 3;

    /**
     * Constant (4) representing April, the fourth month (ISO)
     */
    public static final int APRIL = 4;

    /**
     * Constant (5) representing May, the fifth month (ISO)
     */
    public static final int MAY = 5;

    /**
     * Constant (6) representing June, the sixth month (ISO)
     */
    public static final int JUNE = 6;

    /**
     * Constant (7) representing July, the seventh month (ISO)
     */
    public static final int JULY = 7;

    /**
     * Constant (8) representing August, the eighth month (ISO)
     */
    public static final int AUGUST = 8;

    /**
     * Constant (9) representing September, the nineth month (ISO)
     */
    public static final int SEPTEMBER = 9;

    /**
     * Constant (10) representing October, the tenth month (ISO)
     */
    public static final int OCTOBER = 10;

    /**
     * Constant (11) representing November, the eleventh month (ISO)
     */
    public static final int NOVEMBER = 11;

    /**
     * Constant (12) representing December, the twelfth month (ISO)
     */
    public static final int DECEMBER = 12;

    // These are ints not enumerations as they represent genuine int values
    /**
     * Constant (1) representing Monday, the first day of the week (ISO)
     */
    public static final int MONDAY = 1;

    /**
     * Constant (2) representing Tuesday, the second day of the week (ISO)
     */
    public static final int TUESDAY = 2;

    /**
     * Constant (3) representing Wednesday, the third day of the week (ISO)
     */
    public static final int WEDNESDAY = 3;

    /**
     * Constant (4) representing Thursday, the fourth day of the week (ISO)
     */
    public static final int THURSDAY = 4;

    /**
     * Constant (5) representing Friday, the fifth day of the week (ISO)
     */
    public static final int FRIDAY = 5;

    /**
     * Constant (6) representing Saturday, the sixth day of the week (ISO)
     */
    public static final int SATURDAY = 6;

    /**
     * Constant (7) representing Sunday, the seventh day of the week (ISO)
     */
    public static final int SUNDAY = 7;


    /**
     * Constant (0) representing AM, the morning (from Calendar)
     */
    public static final int AM = 0;

    /**
     * Constant (1) representing PM, the afternoon (from Calendar)
     */
    public static final int PM = 1;


    /**
     * Constant (0) representing BC, years before zero (from Calendar)
     */
    public static final int BC = 0;
    /**
     * Alternative constant (0) representing BCE, Before Common Era (secular)
     */
    public static final int BCE = 0;

    /**
     * Constant (1) representing AD, years after zero (from Calendar).
     * <p>
     * All new chronologies with different Era values should try to assign
     * eras as follows. The era that was in force at 1970-01-01 (ISO) is assigned
     * the value 1. Earlier eras are assigned sequentially smaller numbers.
     * Later eras are assigned sequentially greater numbers.
     */
    public static final int AD = 1;
    /**
     * Alternative constant (1) representing CE, Common Era (secular).
     * <p>
     * All new chronologies with different Era values should try to assign
     * eras as follows. The era that was in force at 1970-01-01 (ISO) is assigned
     * the value 1. Earlier eras are assigned sequentially smaller numbers.
     * Later eras are assigned sequentially greater numbers.
     */
    public static final int CE = 1;


    /**
     * Milliseconds in one second (1000) (ISO)
     */
    public static final int MILLIS_PER_SECOND = 1000;

    /**
     * Seconds in one minute (60) (ISO)
     */
    public static final int SECONDS_PER_MINUTE = 60;
    /**
     * Milliseconds in one minute (ISO)
     */
    public static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;

    /**
     * Minutes in one hour (ISO)
     */
    public static final int MINUTES_PER_HOUR = 60;
    /**
     * Seconds in one hour (ISO)
     */
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * Milliseconds in one hour (ISO)
     */
    public static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR;

    /**
     * Hours in a typical day (24) (ISO). Due to time zone offset changes, the
     * number of hours per day can vary.
     */
    public static final int HOURS_PER_DAY = 24;
    /**
     * Minutes in a typical day (ISO). Due to time zone offset changes, the number
     * of minutes per day can vary.
     */
    public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    /**
     * Seconds in a typical day (ISO). Due to time zone offset changes, the number
     * of seconds per day can vary.
     */
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    /**
     * Milliseconds in a typical day (ISO). Due to time zone offset changes, the
     * number of milliseconds per day can vary.
     */
    public static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;

    /**
     * Days in one week (7) (ISO)
     */
    public static final int DAYS_PER_WEEK = 7;
    /**
     * Hours in a typical week. Due to time zone offset changes, the number of
     * hours per week can vary.
     */
    public static final int HOURS_PER_WEEK = HOURS_PER_DAY * DAYS_PER_WEEK;
    /**
     * Minutes in a typical week (ISO). Due to time zone offset changes, the number
     * of minutes per week can vary.
     */
    public static final int MINUTES_PER_WEEK = MINUTES_PER_DAY * DAYS_PER_WEEK;
    /**
     * Seconds in a typical week (ISO). Due to time zone offset changes, the number
     * of seconds per week can vary.
     */
    public static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;
    /**
     * Milliseconds in a typical week (ISO). Due to time zone offset changes, the
     * number of milliseconds per week can vary.
     */
    public static final int MILLIS_PER_WEEK = MILLIS_PER_DAY * DAYS_PER_WEEK;

    private long TEST_TIME1 =
            (31L + 28L + 31L + 6L - 1L) * MILLIS_PER_DAY
                    + 1L * MILLIS_PER_HOUR
                    + 2L * MILLIS_PER_MINUTE
                    + 3L * MILLIS_PER_SECOND
                    + 4L;
    private long TEST_TIME2 =
            (365L + 31L + 28L + 31L + 30L + 7L - 1L) * MILLIS_PER_DAY
                    + 4L * MILLIS_PER_HOUR
                    + 5L * MILLIS_PER_MINUTE
                    + 6L * MILLIS_PER_SECOND
                    + 7L;


    long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
            366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 +
            365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
            366 + 365;
    long y2003days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
            366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 +
            365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
            366 + 365 + 365;



    // 2002-04-05 Fri
    private long TEST_TIME3 =
            (y2002days + 31L + 28L + 31L + 5L -1L) * MILLIS_PER_DAY
                    + 12L * MILLIS_PER_HOUR
                    + 24L * MILLIS_PER_MINUTE;

    // 2003-05-06 Tue
    private long TEST_TIME4 =
            (y2003days + 31L + 28L + 31L + 30L + 6L -1L) * MILLIS_PER_DAY
                    + 14L * MILLIS_PER_HOUR
                    + 28L * MILLIS_PER_MINUTE;



    @Test
    public void test() {
        System.out.println(TEST_TIME3);
        System.out.println(TEST_TIME4);

        DateTime test1 = new DateTime(TEST_TIME3);
        DateTime test2 = new DateTime(TEST_TIME4);
        assertEquals(true, test2.dayOfWeek().compareTo(test1) < 0);
        assertEquals(true, test1.dayOfWeek().compareTo(test2) > 0);
        assertEquals(true, test1.dayOfWeek().compareTo(test1) == 0);


    }


}