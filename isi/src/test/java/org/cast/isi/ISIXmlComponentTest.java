package org.cast.isi;

import junit.framework.TestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupResourceStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
/**
 * @author heikki doeleman
 */
public class ISIXmlComponentTest extends TestCase {

    @Before
    public void setUp() {}

    @Test
    public void testCreateMarkupFragmentFromString() throws Exception{
        String s = "<span>some markup</span>";
        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);
        MarkupResourceStream markupStream = fragment.getMarkupResourceStream();
        InputStream is = markupStream.getInputStream();
        String x = ISIXmlComponent.getStringFromInputStream(is);
        assertEquals("Expected no changes to input string", s, x);
    }

    // doesn't seem to work? Different version of Junit I guess. If it worked, no need for try/catch/fail in test code.
    // @Test(expected = NullPointerException.class)
    @Test
    public void testCreateMarkupFragmentFromNullString() throws Exception{
        String s = null;
        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);
        try {
            MarkupResourceStream markupStream = fragment.getMarkupResourceStream();
            InputStream is = markupStream.getInputStream();
            String x = ISIXmlComponent.getStringFromInputStream(is);
            fail("Expected NullPointerException didn't happen");
        }
        catch(NullPointerException x) {}
    }

    @Test
    public void testHackUnicodeD7WithoutD7() throws Exception{
        String s = "<span>no unicode d7 here</span>";
        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);

        IMarkupFragment result = ISIXmlComponent.hackUnicodeD7(fragment);
        MarkupResourceStream markupStream = result.getMarkupResourceStream();
        InputStream is = markupStream.getInputStream();
        String result$ = ISIXmlComponent.getStringFromInputStream(is);

        assertEquals("Expected no changes to input string", s, result$);
    }

    @Test
    public void testHackUnicodeD7WithD7() throws Exception{
        String s = "<span>unicode \u00d7 here</span>";
        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);

        IMarkupFragment result = ISIXmlComponent.hackUnicodeD7(fragment);
        MarkupResourceStream markupStream = result.getMarkupResourceStream();
        InputStream is = markupStream.getInputStream();
        String result$ = ISIXmlComponent.getStringFromInputStream(is);

        assertEquals("Expected conversion from unicode D7 to character entity", "<span>unicode &#xd7; here</span>", result$);
    }
}