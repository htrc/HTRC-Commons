/**
 * Copyright 2016 Milinda Pathirage
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.indiana.d2i.htrc.oauth2.filter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class Utils {
  public static Set<String> getHostsFromCommaSeparatedStr(String remoteHosts) {
    if (remoteHosts != null && !remoteHosts.isEmpty()) {
      StringTokenizer tokenizer = new StringTokenizer(remoteHosts, ",");
      Set<String> hosts = new HashSet<>();

      while(tokenizer.hasMoreTokens()) {
        hosts.add(tokenizer.nextToken());
      }

      return hosts;
    }
    return Collections.emptySet();
  }
}
