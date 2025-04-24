print 'hello world'

local android = require 'android'
android.loginfo('lua', 'hello world')

local util = require 'helper.util'
local sp_demo = require 'sp_demo'

function hello()
  return util.factorial4()
end

function test_sp()
  return sp_demo.sp_demo()
end

function clear_sp()
  return sp_demo.clear_prefs()
end

function remove_sp(key)
  return sp_demo.remove_pref(key)
end

function getMyTable()
  local t = {
    a = 'abc',
    b = true,
    [2] = '222',
    [1] = '111',
    c = 0.3,
    d = nil,
    ccc = 0.4,
  }
  return t
end

function testEventBus()
  local eventbus = require 'eventbus'

  eventbus.post('testEvent', {
    data = 'abcdabcd',
  })
  eventbus.register('ping', function(arg)
    android.loginfo('lua', arg)
  end)
end
