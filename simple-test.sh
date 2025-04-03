#!/bin/bash

# Renkli çıktı için
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Sahibinden Test Koşturucu${NC}"
echo -e "${YELLOW}------------------------------------${NC}"

# Test parametrelerini argüman olarak al
TEST_PARAM=${1:-""}
THREAD_COUNT=${2:-"4"}
USE_GRID=${3:-"false"}

# Grid URL ayarla
GRID_URL="http://localhost:4444/wd/hub"

# Test komutunu oluştur
if [ -n "$TEST_PARAM" ]; then
    # Test parametresini hazırla (tırnakları kaldır)
    TEST_PARAM=$(echo "$TEST_PARAM" | sed 's/^"//g' | sed 's/"$//g')
    echo -e "${GREEN}Test:${NC} $TEST_PARAM"
    echo -e "${GREEN}Thread Sayısı:${NC} $THREAD_COUNT"
    echo -e "${GREEN}Selenium Grid:${NC} $([ "$USE_GRID" == "true" ] && echo "Aktif" || echo "Pasif")"
    
    if [ "$USE_GRID" == "true" ]; then
        TEST_CMD="mvn clean test -Dtest=$TEST_PARAM -Dselenium.use.grid=true -Dselenium.grid.url=$GRID_URL -DthreadCount=$THREAD_COUNT"
    else
        TEST_CMD="mvn clean test -Dtest=$TEST_PARAM -DthreadCount=$THREAD_COUNT"
    fi
else
    echo -e "${GREEN}Tüm testler çalıştırılıyor${NC}"
    echo -e "${GREEN}Thread Sayısı:${NC} $THREAD_COUNT"
    echo -e "${GREEN}Selenium Grid:${NC} $([ "$USE_GRID" == "true" ] && echo "Aktif" || echo "Pasif")"
    
    if [ "$USE_GRID" == "true" ]; then
        TEST_CMD="mvn clean test -Dselenium.use.grid=true -Dselenium.grid.url=$GRID_URL -DthreadCount=$THREAD_COUNT"
    else
        TEST_CMD="mvn clean test -DthreadCount=$THREAD_COUNT"
    fi
fi

# Webdriver oturumlarını temizle (varsa ve Grid kullanılmıyorsa)
if [ "$USE_GRID" != "true" ]; then
    echo -e "${GREEN}Mevcut WebDriver oturumları kontrol ediliyor...${NC}"
    if pgrep -f "chromedriver" > /dev/null; then
        echo -e "${YELLOW}Çalışan ChromeDriver işlemleri sonlandırılıyor...${NC}"
        pkill -f "chromedriver" || true
        sleep 1
    fi
fi

# Testleri çalıştır
echo -e "${GREEN}Testler çalıştırılıyor:${NC} $TEST_CMD"
eval $TEST_CMD

# Test sonucunu kontrol et
TEST_RESULT=$?
echo -e "${YELLOW}------------------------------------${NC}"
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}Testler başarıyla tamamlandı!${NC}"
else
    echo -e "${RED}Testlerde hatalar var!${NC}"
fi

# Rapor oluştur
echo -e "${GREEN}Test raporu oluşturuluyor...${NC}"
./create-report.sh 