/* 
 * Copyright 2013 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.perfrepo.rest;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

@Logged
@Interceptor
public class LoggingInterceptor {
   private static final Logger log = Logger.getLogger("org.jboss.qa.perfrepo.REST");

   @AroundInvoke
   public Object logInvocation(InvocationContext context) throws Exception {
      try {
         return context.proceed();
      } catch (SecurityException e) {
         log.error("Error in REST service", e);
         return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
      } catch (Exception e) {
         log.error("Error in REST service", e);
         return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }
}