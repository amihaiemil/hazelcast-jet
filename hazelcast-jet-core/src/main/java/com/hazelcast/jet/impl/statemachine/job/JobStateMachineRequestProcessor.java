/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.impl.statemachine.job;

import com.hazelcast.jet.CombinedJetException;
import com.hazelcast.jet.impl.executor.Task;
import com.hazelcast.jet.impl.job.JobContext;
import com.hazelcast.jet.impl.statemachine.StateMachineRequestProcessor;
import com.hazelcast.jet.job.JobListener;
import java.util.ArrayList;
import java.util.List;

public class JobStateMachineRequestProcessor implements StateMachineRequestProcessor<JobEvent> {
    private final JobContext jobContext;

    public JobStateMachineRequestProcessor(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    private List<Throwable> invokeListeners() {
        List<JobListener> listeners = jobContext.getJobListeners();
        List<Throwable> errors = new ArrayList<Throwable>(listeners.size());

        for (JobListener listener : listeners) {
            try {
                listener.onJobExecuted(jobContext);
            } catch (Throwable e) {
                errors.add(e);
            }
        }

        return errors;
    }

    @Override
    public void processRequest(JobEvent event, Object payload) throws Exception {
        if (event == JobEvent.EXECUTION_START) {
            jobContext.getExecutorContext().getNetworkTasks().forEach(Task::init);
            jobContext.getExecutorContext().getProcessingTasks().forEach(Task::init);
        }

        if ((event == JobEvent.EXECUTION_FAILURE)
                || (event == JobEvent.EXECUTION_SUCCESS)
                || (event == JobEvent.INTERRUPTION_FAILURE)
                || (event == JobEvent.INTERRUPTION_SUCCESS)
                ) {
            try {
                List<Throwable> listeners = invokeListeners();

                if (listeners.size() > 0) {
                    throw new CombinedJetException(listeners);
                }
            } finally {
                jobContext.getExecutorContext().getNetworkTasks().forEach(Task::destroy);
            }
        }
    }
}