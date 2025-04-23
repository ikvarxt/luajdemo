local factorial = require('helper.factorial')

local M = {}

M.getExternalPath = function()
    return android.externalFilePath()
end

M.factorial4 = function()
    return factorial.factorial(4)
end

return M
