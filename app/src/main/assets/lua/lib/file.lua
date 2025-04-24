local M = {}

M.getFileContent = function(path)
  local file = io.open(path, 'r')
  if file == nil then
    error('no file: ' .. path)
  end
  local content = file:read '*a'
  file:close()
  return content
end

---get file's content from android external directory,
---will search under /externalPath/files/*
---@param path string relative path from files
---@return string file content
M.getExternalFileContent = function(path)
  local android = require 'android'
  local externalPath = android.externalFilePath()
  local filePath = externalPath .. '/' .. path
  return M.getFileContent(filePath)
end

return M
