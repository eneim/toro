/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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
 * Created by eneim on 1/30/16.
 */
public class SimpleObject {

  public String name = "GitHub Wikis is a simple way to let others contribute content. "
      + "Any GitHub user can create and edit pages to use for documentation, examples, "
      + "support, or anything you wish.";

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimpleObject that = (SimpleObject) o;

    return name.equals(that.name);
  }

  @Override public int hashCode() {
    return name.hashCode();
  }

  @Override public String toString() {
    return "SimpleObject{" +
        "name='" + name + '\'' +
        '}';
  }
}
