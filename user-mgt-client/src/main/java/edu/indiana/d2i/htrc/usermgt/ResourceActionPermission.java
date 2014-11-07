/*
 * (C) Copyright 2014 Milinda Pathirage.
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
 *
 */

package edu.indiana.d2i.htrc.usermgt;

public enum ResourceActionPermission {

    GET(2),
    PUT(3),
    DELETE(4),
    AUTHORIZE(5);

    private final int _id;

    private ResourceActionPermission(int id) {
        _id = id;
    }

    public String getPermission() {
        return Integer.toString(_id);
    }

    @Override
    public String toString() {
        return getPermission();
    }
}
