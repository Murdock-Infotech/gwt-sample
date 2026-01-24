package murdockinfotech.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import murdockinfotech.shared.ProfileDto;
import murdockinfotech.shared.service.ProfileService;
import murdockinfotech.shared.service.ProfileServiceAsync;

/**
 * Entry point class for the GWT application
 */
public class ModularWebapp implements EntryPoint {
  
  private final ProfileServiceAsync profileService = GWT.create(ProfileService.class);

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
    String serviceUrl = contextRoot + "/" + GWT.getModuleName() + "/profileService";
    ((ServiceDefTarget) profileService).setServiceEntryPoint(serviceUrl);

    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setSpacing(10);
    
    Label title = new Label("Profiles (CRUD)");
    title.setStyleName("title");
    mainPanel.add(title);
    
    Label statusLabel = new Label("Loading...");
    mainPanel.add(statusLabel);

    // Form (create / edit)
    HorizontalPanel formRow = new HorizontalPanel();
    formRow.setSpacing(6);

    Label nameLabel = new Label("Name:");
    TextBox nameBox = new TextBox();
    nameBox.setWidth("240px");

    Button saveButton = new Button("Create");
    Button cancelButton = new Button("Cancel");
    cancelButton.setEnabled(false);

    formRow.add(nameLabel);
    formRow.add(nameBox);
    formRow.add(saveButton);
    formRow.add(cancelButton);
    mainPanel.add(formRow);

    // Table
    FlexTable table = new FlexTable();
    table.setCellSpacing(6);
    table.setText(0, 0, "Id");
    table.setText(0, 1, "Name");
    table.setText(0, 2, "Actions");
    table.getRowFormatter().setStyleName(0, "tableHeader");
    mainPanel.add(table);

    // Local UI state
    final ProfileDto[] editing = new ProfileDto[] { null };

    Runnable resetForm = new Runnable() {
      @Override
      public void run() {
        editing[0] = null;
        nameBox.setText("");
        saveButton.setText("Create");
        cancelButton.setEnabled(false);
      }
    };

    ClickHandler cancelHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        resetForm.run();
        statusLabel.setText("Ready");
      }
    };
    cancelButton.addClickHandler(cancelHandler);

    final Runnable[] refresh = new Runnable[] { null };
    refresh[0] = new Runnable() {
      @Override
      public void run() {
        statusLabel.setText("Loading...");
        profileService.listProfiles(new AsyncCallback<List<ProfileDto>>() {
          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setText("Error: " + caught.getMessage());
          }

          @Override
          public void onSuccess(List<ProfileDto> result) {
            // Clear all rows except header.
            while (table.getRowCount() > 1) {
              table.removeRow(1);
            }

            int row = 1;
            for (final ProfileDto p : result) {
              table.setText(row, 0, p.getId() == null ? "" : String.valueOf(p.getId()));
              table.setText(row, 1, p.getName() == null ? "" : p.getName());

              HorizontalPanel actions = new HorizontalPanel();
              actions.setSpacing(4);

              Button editBtn = new Button("Edit");
              Button deleteBtn = new Button("Delete");

              editBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  editing[0] = new ProfileDto(p.getId(), p.getName());
                  nameBox.setText(p.getName() == null ? "" : p.getName());
                  saveButton.setText("Save");
                  cancelButton.setEnabled(true);
                  statusLabel.setText("Editing id " + p.getId());
                }
              });

              deleteBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  if (!Window.confirm("Delete profile '" + (p.getName() == null ? "" : p.getName()) + "'?")) {
                    return;
                  }
                  statusLabel.setText("Deleting...");
                  profileService.deleteProfile(p.getId(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      statusLabel.setText("Error: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void ignored) {
                      resetForm.run();
                      refresh[0].run();
                      statusLabel.setText("Deleted");
                    }
                  });
                }
              });

              actions.add(editBtn);
              actions.add(deleteBtn);
              table.setWidget(row, 2, actions);

              row++;
            }

            statusLabel.setText("Ready (" + result.size() + " profiles)");
          }
        });
      }
    };

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final String name = nameBox.getText();
        if (name == null || name.trim().isEmpty()) {
          Window.alert("Name is required");
          return;
        }

        if (editing[0] == null) {
          statusLabel.setText("Creating...");
          profileService.createProfile(name.trim(), new AsyncCallback<ProfileDto>() {
            @Override
            public void onFailure(Throwable caught) {
              statusLabel.setText("Error: " + caught.getMessage());
            }

            @Override
            public void onSuccess(ProfileDto created) {
              resetForm.run();
              refresh[0].run();
              statusLabel.setText("Created id " + (created == null ? "" : created.getId()));
            }
          });
        } else {
          statusLabel.setText("Saving...");
          ProfileDto toSave = new ProfileDto(editing[0].getId(), name.trim());
          profileService.updateProfile(toSave, new AsyncCallback<ProfileDto>() {
            @Override
            public void onFailure(Throwable caught) {
              statusLabel.setText("Error: " + caught.getMessage());
            }

            @Override
            public void onSuccess(ProfileDto updated) {
              resetForm.run();
              refresh[0].run();
              statusLabel.setText("Saved id " + (updated == null ? "" : updated.getId()));
            }
          });
        }
      }
    });
    
    // Render into a known element if it exists; otherwise, fall back to the <body>.
    RootPanel target = RootPanel.get("gwtRoot");
    if (target == null) {
      target = RootPanel.get();
    }
    target.add(mainPanel);

    // Initial load
    refresh[0].run();
  }
}

