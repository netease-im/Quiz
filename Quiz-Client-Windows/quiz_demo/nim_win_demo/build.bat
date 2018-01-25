echo build begin

path C:\Program Files (x86)\Microsoft Visual Studio 12.0\Common7\IDE;%path%
devenv nim.sln /build Release /project "nim_demo" /projectconfig Release

echo build end
pause 