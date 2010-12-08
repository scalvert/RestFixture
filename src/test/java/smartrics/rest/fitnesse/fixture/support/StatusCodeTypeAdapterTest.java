/*  Copyright 2008 Fabrizio Cannizzo
 *
 *  This file is part of RestFixture.
 *
 *  RestFixture (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  RestFixture is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with RestFixture.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please leave a comment here
 *  http://smartrics.blogspot.com/2008/08/get-fitnesse-with-some-rest.html
 */
package smartrics.rest.fitnesse.fixture.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatusCodeTypeAdapterTest {

	private StatusCodeTypeAdapter adapter = new StatusCodeTypeAdapter();

	@Test
	public void shouldParseStatusCodesAsTrimmedStrings() {
		assertEquals("100", (String)adapter.parse(" 100 "));
		assertEquals("null", (String)adapter.parse(null));
	}

	@Test
	public void shouldRenderCellContentAsStrings(){
		assertEquals("200", adapter.toString("200"));
		assertEquals("blank", adapter.toString(" "));
		assertEquals("null", adapter.toString(null));
	}

	@Test
	public void expectedShouldBeTreatedAsRegularExpression(){
		assertTrue("expected is not treated as regular expression", adapter.equals("20\\d", "201"));
		assertTrue("expected is not treated as regular expression", adapter.equals("20\\d", "202"));
	}

	@Test
	public void shouldNotEqualiseIfExpectedOrActualAreNull(){
		assertFalse(adapter.equals(null, "201"));
		assertFalse(adapter.equals("20\\d", null));
	}

	@Test
	public void whenExpectedIsNotMatchedAnErrorShouldBeAdded(){
		adapter.equals("20\\d", "404");
		assertEquals(1, adapter.getErrors().size());
		assertEquals("not match: 20\\d", adapter.getErrors().get(0));
	}

}
