package murdockinfotech.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.Window;
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

  private static native String getConfiguredContextRoot() /*-{
    return ($wnd && $wnd.__MODULAR_WEBAPP_CONTEXT_ROOT__) ? $wnd.__MODULAR_WEBAPP_CONTEXT_ROOT__ : null;
  }-*/;

  private static String normalizeNoTrailingSlash(String value) {
    if (value == null) {
      return null;
    }
    String v = value.trim();
    while (v.endsWith("/")) {
      v = v.substring(0, v.length() - 1);
    }
    return v.isEmpty() ? null : v;
  }
  
  public void onModuleLoad() {
    // RPC endpoint:
    // - Prefer a value provided by Spring Boot (via /client-config.js, backed by application.properties)
    // - Fall back to the current browser origin
    //
    // Final URL: {contextRoot}/{moduleName}/userService
    String contextRoot = normalizeNoTrailingSlash(getConfiguredContextRoot());
    if (contextRoot == null) {
      contextRoot = Window.Location.getProtocol() + "//" + Window.Location.getHost();
    }
    GWT.log("contextRoot: " + contextRoot);
    String serviceUrl = contextRoot + "/" + GWT.getModuleName() + "/userService";
    ((ServiceDefTarget) userService).setServiceEntryPoint(serviceUrl);

    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setSpacing(10);
    
    Label title = new Label("Modular Web Application 7");
    title.setStyleName("title");
    mainPanel.add(title);
    
    Label statusLabel = new Label("Ready");
    mainPanel.add(statusLabel);
    
    Button testButton = new Button("Test Server Connection Client 4");
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

