package com.mergebase.push0ver;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

/*
 * Logic to test:
 *
 *   1.) If the current HEAD commit contains one or more tags, return the "largest" one.
 *       Note:  1.11 is considered larger than 1.9, and 0.0.1 is considered larger than 999.999.999-SNAPSHOT.
 *
 *   2.) If the current HEAD does not contain any tags, go back in history to find the most
 *       recent tag.  If the most recent tag is a "*-SNAPSHOT" tag, return that.  If
 *       the most recent tag is a regular release tag (e.g., no occurrence of the word "SNAPSHOT"),
 *       then return null.
 */
public class TagExtractorTest {
    final static String CASE_2A =
            "\n" +
                    " (tag: test)\n" +
                    "\n" +
                    " (tag: 0.0.1-SNAPSHOT)\n" +
                    " (tag: 0.0.2-SNAPSHOT)\n" +
                    " (tag: abc, tag: 0.0.1, tag: 0.0.0.1, tag: 0.0.0.0.1)\n";

    final static String CASE_2B =
            "\n" +
                    " (tag: test)\n" +
                    "\n" +
                    " (tag: 0.0.1b)\n" +
                    " (tag: 0.0.2-SNAPSHOT)\n" +
                    " (tag: abc, tag: 0.0.1, tag: 0.0.0.1, tag: 0.0.0.0.1)\n";

    final static String CASE_2C =
            "\n" +
                    " (tag: test)\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    " (tag: abc, tag: 0.0.1, tag: 0.0.0.1, tag: 0.0.0.0.1, tag: 99-SNAPSHOT)\n";

    final static String CASE_2D =
            "\n" +
                    " (tag: test)\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    " (tag: abc, tag: 99, tag: 0.0.1, tag: 0.0.0.1, tag: 0.0.0.0.1, tag: 99-SNAPSHOT)\n";

    final static String CASE_2E =
            "\n" +
                    " (tag: test)\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    " (tag: abc, tag: 100.0.0, tag: 0.0.1, tag: 0.0.0.1, tag: 0.0.0.0.1, tag: 99-SNAPSHOT)\n";

    final static String CASE_1A =
            " (HEAD -> master, origin/master, origin/HEAD, tag: 1.1.1.2, tag: 1.1.1, tag: 1.1.1.2a, tag: 99.99-SNAPSHOT, tag: 1.0, tag: zzz)\n" +
                    CASE_2A;

    final static String CASE_1B =
            " (HEAD -> master, origin/master, origin/HEAD, tag: 99.99-SNAPSHOT, tag: 1.0-SNAPSHOT, tag: zzz)\n" + CASE_2A;

    final static String CASE_NOT_SO_BAD =
            " (tag: abc, tag: a/100.0.0, tag: b/0.0.1, tag: c/d/0.0.0.1, tag: e/f/0.0.0.0.1, tag: e/0.0.0.0.1)\n";

    final static String CASE_BAD =
            " (tag: abc, tag: a/100.0.0, tag: a/b/0.0.1, tag: c/d/0.0.0.1, tag: e/f/0.0.0.0.1, tag: 99-SNAPSHOT)\n";

    @Test
    public void testBadTagCombo() {
        StringReader r = new StringReader(CASE_NOT_SO_BAD);
        Map<String, Tag> m = TagExtractor.extractTagTestLogicFull(r);

        Assert.assertEquals("/a/100.0.0", m.get("/a").toString());
        Assert.assertEquals("/b/0.0.1", m.get("/b").toString());
        Assert.assertEquals("/c/d/0.0.0.1", m.get("/c/d").toString());
        Assert.assertEquals("/e/0.0.0.0.1", m.get("/e").toString());
        Assert.assertEquals(4, m.size());


        r = new StringReader(CASE_BAD);
        String msg = "Oops, no Exception was thrown!";
        try {
            TagExtractor.extractTagTestLogic(r);
        } catch (IllegalArgumentException iae) {
            msg = iae.getMessage();
        }
        Assert.assertEquals("Incompatible parent/child tag combo. /a=100.0.0 but /a/b=0.0.1", msg);
    }

    @Test
    public void testCases() {
        // Case 1a.) Largest non-snapshot tag in HEAD is "1.1.1.2a":
        StringReader r = new StringReader(CASE_1A);
        Assert.assertEquals("1.1.1.2", TagExtractor.extractTagTestLogic(r));

        // Case 1b.) Largest tag in HEAD is "99.99-SNAPSHOT".  All tags in HEAD contain "-SNAPSHOT".
        // (Except for tag "zzz" which is ignored because it's invalid).
        r = new StringReader(CASE_1B);
        Assert.assertEquals("99.99-SNAPSHOT", TagExtractor.extractTagTestLogic(r));

        // Case 2a.) No tags in HEAD.  Go back in history and nearest tag is "0.0.1-SNAPSHOT".
        r = new StringReader(CASE_2A);
        Assert.assertEquals("0.0.1-SNAPSHOT", TagExtractor.extractTagTestLogic(r));

        // Case 2b.) No tags in HEAD.  Go back in history and nearest tag is "0.0.1b", which is
        // not a SNAPSHOT and so null is returned.
        r = new StringReader(CASE_2B);
        Assert.assertNull(TagExtractor.extractTagTestLogic(r));

        // Case 2c.) No tags in HEAD.  Go back in history and nearest tag is "0.0.1b", which is
        // not a SNAPSHOT and so null is returned EXCEPT that same commit also has 99-SNAPSHOT,
        // and so 99-SNAPSHOT is returned.
        r = new StringReader(CASE_2C);
        Assert.assertEquals("99-SNAPSHOT", TagExtractor.extractTagTestLogic(r));

        // Case 2d.) and 2e.) No tags in HEAD.  Go back in history and nearest tag is "99", which is
        // not a SNAPSHOT and so null is returned EXCEPT that same commit also has 99-SNAPSHOT,
        // and so 99-SNAPSHOT would be returned, except that SNAPSHOT must be larger than release
        // tag, and so null is returned.
        r = new StringReader(CASE_2D);

        // 99 vs. 99-SNAPSHOT
        Assert.assertNull(TagExtractor.extractTagTestLogic(r));

        // 100.0.0 vs. 99-SNAPSHOT
        r = new StringReader(CASE_2E);
        Assert.assertNull(TagExtractor.extractTagTestLogic(r));

        // The nothing-interesting case.  No tags at all!
        r = new StringReader("\n\n\n\n\n\n\n\n");
        Assert.assertNull(TagExtractor.extractTagTestLogic(r));
    }

    public static void main(String[] args) {
        TagExtractorTest t = new TagExtractorTest();

        t.testCases();
    }
}
