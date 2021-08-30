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

import static js.base.Tools.*;

import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import js.file.Files;
import js.json.JSList;
import js.json.JSMap;

public final class Util {

  public static JSMap toJson(HttpExchange t) {
    JSMap m = map();
    m.putNumbered("method", t.getRequestMethod())//
        .putNumbered("protocol", t.getProtocol())//
        .putNumbered("req URI", t.getRequestURI().toString())//
    ;
    if (false)
      m.putNumbered("req headers", toJson(t.getRequestHeaders()));
    return m;
  }

  public static JSMap toJson(Headers headers) {
    JSMap m = map();
    for (Entry<String, List<String>> ent : headers.entrySet())
      m.put(ent.getKey(), JSList.withUnsafeList(ent.getValue()));
    return m;
  }

  public static JSMap readJsonRequestBody(HttpExchange t) {
    String body = Files.readString(t.getRequestBody());
    if (body.isEmpty())
      return JSMap.DEFAULT_INSTANCE;
    return new JSMap(body);
  }

}
