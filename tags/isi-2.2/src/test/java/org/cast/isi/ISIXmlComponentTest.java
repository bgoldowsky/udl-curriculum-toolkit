/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the UDL Curriculum Toolkit:
 * see <http://code.google.com/p/udl-curriculum-toolkit>.
 *
 * The UDL Curriculum Toolkit is free software: you can redistribute and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The UDL Curriculum Toolkit is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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
// removed these because it was removed from code - ldm
//    @Test
//    public void testHackUnicodeD7WithoutD7() throws Exception{
//        String s = "<span>no unicode d7 here</span>";
//        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);
//
//        IMarkupFragment result = ISIXmlComponent.hackUnicodeD7(fragment);
//        MarkupResourceStream markupStream = result.getMarkupResourceStream();
//        InputStream is = markupStream.getInputStream();
//        String result$ = ISIXmlComponent.getStringFromInputStream(is);
//
//        assertEquals("Expected no changes to input string", s, result$);
//    }
//
//    @Test
//    public void testHackUnicodeD7WithD7() throws Exception{
//        String s = "<span>unicode \u00d7 here</span>";
//        IMarkupFragment fragment = ISIXmlComponent.createMarkupFragmentFromString(s);
//
//        IMarkupFragment result = ISIXmlComponent.hackUnicodeD7(fragment);
//        MarkupResourceStream markupStream = result.getMarkupResourceStream();
//        InputStream is = markupStream.getInputStream();
//        String result$ = ISIXmlComponent.getStringFromInputStream(is);
//
//        assertEquals("Expected conversion from unicode D7 to character entity", "<span>unicode &#xd7; here</span>", result$);
//    }
}