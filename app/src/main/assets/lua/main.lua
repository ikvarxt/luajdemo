print 'hello world'

local android = require 'android'
android.loginfo('lua', 'hello world')

local util = require 'helper.util'
local sp_demo = require 'sp_demo'
local eventbus = require 'eventbus'

function hello()
  return util.factorial4()
end

function test_sp()
  return sp_demo.sp_demo()
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
  eventbus.post('ui', { data = 'abcdabcd' })
  eventbus.register('ping', function(arg)
    android.loginfo('lua', arg)
  end)

  for i = 1, 2 do
    eventbus.post('ui', {
      data = 'text from lua i=' .. i,
    })
  end
end

function readAndroidFile()
  local externalPath = android.externalFilePath()
  local fullPath = externalPath .. '/lua/text.lua'
  local textLua = io.open(fullPath, 'r')
  if textLua == nil then
    eventbus.post('ui', { data = 'not found file ' .. fullPath })
    return
  end
  local t = textLua:read '*a'
  textLua:close()
  eventbus.post('ui', { data = t })
end

function TEST_JSON_ENCODE()
  local json = require 'lib.json'

  local t = {
    'a',
    'bbb',
    'ccc',
    'ddd',
  }
  return json.encode(t)
end

function TEST_JSON_DECODE()
  local json = require 'lib.json'
  local j = '{"a":"abc", "b": true}'
  return json.decode(j)
end

function TEST_FILE_TO_JSON()
  local json = require 'lib.json'
  local file = require 'lib.file'

  local content = file.getExternalFileContent 'lua/resource/t.json'
  eventbus.post('ui', { content = content })

  return json.decode(content)
end
