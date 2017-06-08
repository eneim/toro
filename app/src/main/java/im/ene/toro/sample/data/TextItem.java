/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.data;

/**
 * @author eneim | 6/7/17.
 */

public class TextItem implements Entity {

  private final String content = "# About GitHub Wikis\n"
      + "> Just as writing good code and great tests are important, excellent documentation helps others use and extend your project.\n"
      + "\n"
      + "> Every GitHub repository comes equipped with a section for hosting documentation, called a wiki.";

  TextItem() {
  }

  @Override public String toString() {
    return "TextItem{" + hashCode() + "}";
  }

  public String getContent() {
    return content;
  }
}
