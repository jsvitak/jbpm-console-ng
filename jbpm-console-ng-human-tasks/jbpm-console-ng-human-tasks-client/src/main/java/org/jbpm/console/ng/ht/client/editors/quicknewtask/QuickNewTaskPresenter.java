/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.ht.client.editors.quicknewtask;


import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import java.util.Date;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;


import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.ht.service.TaskServiceEntryPoint;
import org.uberfire.client.annotations.OnReveal;
import org.uberfire.client.annotations.OnStart;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.events.BeforeClosePlaceEvent;
import org.uberfire.security.Identity;
import org.uberfire.shared.mvp.PlaceRequest;

@Dependent
@WorkbenchPopup(identifier = "Quick New Task")
public class QuickNewTaskPresenter {

    public interface QuickNewTaskView
            extends
            UberView<QuickNewTaskPresenter> {

        void displayNotification(String text);

        TextBox getTaskNameText();
        
        Button getAddTaskButton();
    }
    @Inject
    QuickNewTaskView view;
    @Inject
    Identity identity;
    @Inject
    Caller<TaskServiceEntryPoint> taskServices;
    @Inject
    private Event<BeforeClosePlaceEvent> closePlaceEvent;
    private PlaceRequest place;
    
    @Inject
    private PlaceManager placeManager;

    @OnStart
    public void onStart(final PlaceRequest place) {
        this.place = place;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Quick New Task";
    }

    @WorkbenchPartView
    public UberView<QuickNewTaskPresenter> getView() {
        return view;
    }

    public QuickNewTaskPresenter() {
    }

    @PostConstruct
    public void init() {
    }

    public void addTask(final String userId, String taskName, boolean isQuickTask,  Date due) {
        String str = "(with (new Task()) { taskData = (with( new TaskData()) { expirationTime = new java.util.Date() } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = ";
        if (userId != null && !userId.equals("")) {
            str += " [new User('" + userId + "')  ], }),";
        }
        str += "names = [ new I18NText( 'en-UK', '" + taskName + "')]})";
        if(isQuickTask){
            taskServices.call(new RemoteCallback<Long>() {
                @Override
                public void callback(Long taskId) {
                    view.displayNotification("Task Created and Started (id = " + taskId + ")");
                    close();
                    
                }
            }).addTaskAndStart(str, null, identity.getName() );
        }else{
            taskServices.call(new RemoteCallback<Long>() {
                @Override
                public void callback(Long taskId) {
                    view.displayNotification("Task Created (id = " + taskId + ")");
                    close();
                    
                }
            }).addTask(str, null);
        }

    }

    @OnReveal
    public void onReveal() {
        view.getTaskNameText().setFocus(true);

    }

    public void close() {
        closePlaceEvent.fire(new BeforeClosePlaceEvent(this.place));
    }
}
