echo build begin

path C:\Program Files (x86)\Microsoft Visual Studio 12.0\Common7\IDE;%path%
devenv nim.sln /build Debug /project "nim_demo" /projectconfig Debug

echo build end
pause 