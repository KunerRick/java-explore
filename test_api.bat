@echo off
echo Testing Async Task Manager API...

echo.
echo 1. Creating a video scoring task...
curl -X POST http://localhost:8080/api/example/video-scoring -H "Content-Type: application/json" -d "{\"videoId\": \"1001\", \"videoName\": \"测试视频1\"}"

echo.
echo 2. Creating another video scoring task (without subtitle)...
curl -X POST http://localhost:8080/api/example/video-scoring -H "Content-Type: application/json" -d "{\"videoId\": \"1002\", \"videoName\": \"测试视频2\"}"

echo.
echo 3. Checking task statistics...
curl http://localhost:8080/api/tasks/statistics

pause