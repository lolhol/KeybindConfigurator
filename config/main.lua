function abc(i)
    return i + 1
end

function test()
    return 5 + abc(5)
end

function CustomXTest()
    return "test", { 1, 2, 3, 4 }, test()
end

return {
    CustomXTest = CustomXTest
}