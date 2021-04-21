-- Insert super user API key
INSERT INTO authentication_keys (id, key_value, insert_instant, key_manager, last_update_instant, permissions)
  VALUES (MD5(RANDOM() :: TEXT || CLOCK_TIMESTAMP() :: TEXT) :: UUID, 'bf69486b-4733-4470-a592-f1bfce7af580', 0, FALSE, 0, NULL);