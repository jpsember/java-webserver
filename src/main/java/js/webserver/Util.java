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
