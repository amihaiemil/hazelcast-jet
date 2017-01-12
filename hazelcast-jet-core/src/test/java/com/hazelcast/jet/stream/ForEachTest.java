/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.stream;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ForEachTest extends AbstractStreamTest {

    @Test
    public void testForEach_whenSourceMap() {
        IStreamMap<String, Integer> map = getMap();
        fillMap(map);

        final AtomicInteger runningTotal = new AtomicInteger(0);
        map.stream().forEach(e -> runningTotal.addAndGet(e.getValue()));

        assertEquals((COUNT - 1) * (COUNT) / 2, runningTotal.get());
    }

    @Test
    public void testForEach_whenSourceList() {
        IStreamList<Integer> list = getList();
        fillList(list);

        final AtomicInteger runningTotal = new AtomicInteger(0);
        list.stream().forEach(runningTotal::addAndGet);

        assertEquals((COUNT - 1) * (COUNT) / 2, runningTotal.get());
    }

}