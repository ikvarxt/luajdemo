print 'hello world'

local android = require 'android'
android.loginfo('lua', 'hello world')

local util = require 'helper.util'

function hello()
  return util.factorial4()
end
