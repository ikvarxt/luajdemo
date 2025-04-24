-- SharedPreferences Demo
local sp = require 'sp'

-- Function to demonstrate SharedPreferences usage
local function sp_demo()
  -- Store values
  sp.setString('username', 'JohnDoe')
  sp.setInt('age', 25)
  sp.setBoolean('is_logged_in', true)
  sp.setFloat('balance', 100.50)

  -- Retrieve values
  local username = sp.getString('username', '')
  local age = sp.getInt('age', 0)
  local is_logged_in = sp.getBoolean('is_logged_in', false)
  local balance = sp.getFloat('balance', 0.0)

  -- Format the result
  local result = string.format('Username: %s\nAge: %d\nLogged in: %s\nBalance: %.2f', username, age, tostring(is_logged_in), balance)

  -- Return the result
  return result
end

-- Function to clear all preferences
local function clear_prefs()
  sp.clear()
  return 'All preferences cleared'
end

-- Function to remove a specific preference
local function remove_pref(key)
  sp.remove(key)
  return string.format("Preference '%s' removed", key)
end

return {
  sp_demo = sp_demo,
  clear_prefs = clear_prefs,
  remove_pref = remove_pref,
}
