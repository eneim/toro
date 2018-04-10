/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package toro.demo.exoplayer.basic;

import android.content.Context;
import android.support.v4.util.Pair;
import io.reactivex.Observable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import toro.demo.exoplayer.DemoApp;

/**
 * Parsing some html for the demo app.
 *
 * @author eneim (2018/01/23).
 */

public class Motion {

  static final String BODY_SELECTOR = "#grid-cont > section";

  static final Document article;

  static final String INTRO =
      "#grid-cont > section > div > div.article-content.chapter-intro > div";
  static final String ID_PART1 = "material-motion-why-does-motion-matter";
  static final String ID_PART2 = "material-motion-how-does-material-move";
  static final String ID_PART3 = "material-motion-what-makes-a-good-transition";
  static final String ID_PART4 = "material-motion-implications-of-motion";

  static {
    article = Jsoup.parse(getFileContent(DemoApp.Companion.getDemoApp(), "motion/index.html"));
  }

  @SuppressWarnings("SameParameterValue")
  private static String getFileContent(Context context, String fileName) {
    StringBuilder total = new StringBuilder();
    try (InputStream inputStream = context.getAssets().open(fileName);
         BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = r.readLine()) != null) {
        total.append(line).append('\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return total.toString();
  }

  public static Observable<ArrayList<Pair<String, Element>>> contents() {
    return Observable.fromCallable(() -> {
      Element body = article.body();
      ArrayList<Pair<String, Element>> result = new ArrayList<>();
      result.add(Pair.create("", body.select(INTRO).first()));

      Element part;

      part = body.getElementById(ID_PART2);
      result.add(Pair.create(part.child(0).text(), part.child(1)));

      part = body.getElementById(ID_PART3);
      result.add(Pair.create(part.child(0).text(), part.child(1)));

      part = body.getElementById(ID_PART4);
      result.add(Pair.create(part.child(0).text(), part.child(1)));
      return result;
    });
  }

  // try hard to flatten elements from the source into des
  public static void flatten(Elements des, Element source) {
    Elements children = source.children();
    if (children.size() == 0 || //
        "media".equals(source.className()) || //
        "figcaption".equals(source.tagName()) ||  //
        "module".equals(source.className())) {
      des.add(source);
    } else {
      for (Element element : children) {
        flatten(des, element);
      }
    }
  }
}
