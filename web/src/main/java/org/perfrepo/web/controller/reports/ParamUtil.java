/**
 * PerfRepo
 * <p>
 * Copyright (C) 2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.perfrepo.web.controller.reports;

import org.perfrepo.model.TestExecution;

import java.util.ArrayList;
import java.util.List;

public class ParamUtil {

   private ParamUtil() { }

   public static List<Long> parseExecQuery(String query) {
      try {
         if (query == null) {
            return null;
         }
         String[] idStrings = query.split("\\||-");
         List<Long> r = new ArrayList<Long>(idStrings.length);
         for (String idString : idStrings) {
            r.add(new Long(idString));
         }
         return r.isEmpty() ? null : r;
      } catch (Exception e) {
         return null;
      }
   }

   public static String generateExecQuery(List<TestExecution> execs) {
      if (execs == null || execs.isEmpty()) {
         return null;
      }
      StringBuffer s = new StringBuffer(execs.get(0).getId().toString());
      for (int i = 1; i < execs.size(); i++) {
         s.append("-");
         s.append(execs.get(i).getId());
      }
      return s.toString();
   }
}
