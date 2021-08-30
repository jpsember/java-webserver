/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.webserver;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import js.base.BaseObject;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

import static js.base.Tools.*;

public final class UriParser extends BaseObject {

  public UriParser(String pathPrefix, URI uri) {
    mPathPrefix = pathPrefix;
    mUri = uri;
  }

  public boolean valid() {
    if (mValid == null)
      auxParse();
    return mValid;
  }

  public List<String> elements() {
    valid();
    return mElements;
  }

  private static List<String> removeEmptyStrings(Collection<String> strings) {
    return strings.stream().filter((x) -> !x.isEmpty()).collect(Collectors.toList());
  }

  private void auxParse() {
    mValid = false;
    mElements = DataUtil.emptyList();
    String p = mUri.getPath();

    mQuery = nullToEmpty(mUri.getQuery());
    mFragment = nullToEmpty(mUri.getFragment());

    if (mQuery.length() + mFragment.length() != 0) {
      todo("queries (?) and fragments (#) aren't supported");
      return;
    }

    String p2 = chompPrefix(p, mPathPrefix);
    if (p == p2 || (!p2.isEmpty() && p2.charAt(0) != '/'))
      return;

    mElements = removeEmptyStrings(split(p2, '/'));

    mValid = true;
  }

  @Override
  public JSMap toJson() {
    JSMap m = map();
    m.put("path", mUri.getPath());
    m.put("prefix", mPathPrefix);
    m.put("valid", valid());
    m.put("elements", JSList.withUnsafeList(mElements));
    m.put("query", mQuery);
    m.put("fragment", mFragment);
    return m;
  }

  private final URI mUri;
  private String mPathPrefix;
  private Boolean mValid;
  private List<String> mElements;
  private String mQuery;
  private String mFragment;
}
