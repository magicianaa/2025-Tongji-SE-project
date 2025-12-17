# 测试预订功能流程
# 1. 用户登录
# 2. 创建预订（包含主住客姓名）
# 3. 查看房态列表（应显示已预订）
# 4. 办理入住（验证预订信息）

$baseUrl = "http://localhost:8080/api"

# Step 1: 用户登录
Write-Host "`n===== Step 1: 用户登录 =====" -ForegroundColor Cyan
$loginBody = @{
    username = "15800000001"
    password = "Test123456"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token
Write-Host "登录成功，Token: $($token.Substring(0,20))..." -ForegroundColor Green

# Step 2: 创建预订
Write-Host "`n===== Step 2: 创建预订（房间203） =====" -ForegroundColor Cyan
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$dayAfterTomorrow = (Get-Date).AddDays(2).ToString("yyyy-MM-dd")

$bookingBody = @{
    roomId = 3  # 203房间
    plannedCheckin = $tomorrow
    plannedCheckout = $dayAfterTomorrow
    mainGuestName = "张三"  # 主住客姓名（必填）
    specialRequests = "测试预订功能"
} | ConvertTo-Json

try {
    $bookingResponse = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $bookingBody -ContentType "application/json" -Headers @{Authorization="Bearer $token"}
    Write-Host "预订成功！" -ForegroundColor Green
    Write-Host "预订ID: $($bookingResponse.data.bookingId)"
    Write-Host "房间号: $($bookingResponse.data.roomNo)"
    Write-Host "主住客: $($bookingResponse.data.mainGuestName)"
    Write-Host "入住日期: $($bookingResponse.data.plannedCheckin)"
    Write-Host "退房日期: $($bookingResponse.data.plannedCheckout)"
    Write-Host "状态: $($bookingResponse.data.status)"
} catch {
    Write-Host "预订失败: $_" -ForegroundColor Red
    $_.Exception.Response | ConvertFrom-Json
}

# Step 3: 查看房态列表（等待后端重启）
Write-Host "`n===== Step 3: 等待后端服务重启... =====" -ForegroundColor Cyan
Start-Sleep -Seconds 15

Write-Host "`n===== Step 4: 查看房态列表 =====" -ForegroundColor Cyan
try {
    $roomsResponse = Invoke-RestMethod -Uri "$baseUrl/rooms" -Method GET -Headers @{Authorization="Bearer $token"}
    
    # 查找203房间
    $room203 = $roomsResponse.data | Where-Object { $_.roomNo -eq "203" }
    if ($room203) {
        Write-Host "房间203状态:" -ForegroundColor Yellow
        Write-Host "  房间号: $($room203.roomNo)"
        Write-Host "  状态: $($room203.status)"
        Write-Host "  是否已预订: $($room203.hasBooking)"
        if ($room203.hasBooking) {
            Write-Host "  预订ID: $($room203.bookingId)" -ForegroundColor Green
            Write-Host "  预订状态: $($room203.bookingStatus)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "查询房态失败: $_" -ForegroundColor Red
}

# Step 5: 测试办理入住（使用预订的主住客姓名）
Write-Host "`n===== Step 5: 测试办理入住（验证预订信息） =====" -ForegroundColor Cyan
$checkInBody = @{
    roomNo = "203"
    expectedCheckout = $dayAfterTomorrow + "T12:00:00"
    guests = @(
        @{
            phone = "15800000001"
            realName = "张三"  # 与预订时的主住客姓名一致
            idCard = "110101199001011234"
        }
    )
} | ConvertTo-Json

try {
    $checkInResponse = Invoke-RestMethod -Uri "$baseUrl/check-in" -Method POST -Body $checkInBody -ContentType "application/json" -Headers @{Authorization="Bearer $token"}
    Write-Host "入住成功！预订验证通过" -ForegroundColor Green
    Write-Host "房间号: $($checkInResponse.data.roomNo)"
    Write-Host "入住时间: $($checkInResponse.data.checkInTime)"
} catch {
    Write-Host "入住失败: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host $responseBody -ForegroundColor Red
    }
}

Write-Host "`n===== 测试完成 =====" -ForegroundColor Cyan
