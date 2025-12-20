package murdockinfotech.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import murdockinfotech.shared.UserDto;
import murdockinfotech.shared.service.UserService;
import murdockinfotech.shared.service.UserServiceAsync;

/**
 * Entry point class for the GWT application
 */
public class ModularWebapp implements EntryPoint {
  
  private final UserServiceAsync userService = GWT.create(UserService.class);
  
  public void onModuleLoad() {
    // Ensure the RPC endpoint matches the server servlet mapping.
    // With the GWT module at /modularwebapp/, this resolves to /modularwebapp/userService
    ((ServiceDefTarget) userService).setServiceEntryPoint(GWT.getModuleBaseURL() + "userService");

    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setSpacing(10);
    
    Label title = new Label("Modular Web Application");
    title.setStyleName("title");
    mainPanel.add(title);
    
    Label statusLabel = new Label("Ready");
    mainPanel.add(statusLabel);
    
    Button testButton = new Button("Test Server Connection Client");
    testButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        statusLabel.setText("Calling server...");
        userService.greetUser(new UserDto("Test User", "test@example.com"), new AsyncCallback<String>() {
          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setText("Error: " + caught.getMessage());
          }
          
          @Override
          public void onSuccess(String result) {
            statusLabel.setText("Server response: " + result);
          }
        });
      }
    });
    mainPanel.add(testButton);
    
    // Render into a known element if it exists; otherwise, fall back to the <body>.
    RootPanel target = RootPanel.get("gwtRoot");
    if (target == null) {
      target = RootPanel.get();
    }
    target.add(mainPanel);
  }
}

