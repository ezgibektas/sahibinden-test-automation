#!/bin/bash

# Renkli çıktı için
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Sahibinden Test Raporu Oluşturucu${NC}"
echo -e "${YELLOW}------------------------------------${NC}"

# Rapor dosyasını oluştur
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
REPORT_DIR="$HOME/test-reports"
REPORT_PATH="$REPORT_DIR/test-report-$TIMESTAMP.html"

# Dizin oluştur
mkdir -p "$REPORT_DIR"

# Surefire raporlarını kontrol et
SUREFIRE_DIR="target/surefire-reports"
SCREENSHOT_DIR="target/screenshots"

# HTML rapor başlığı
cat > "$REPORT_PATH" << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Test Raporu - $TIMESTAMP</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            margin: 0;
            padding: 20px;
            background-color: #f8f9fa;
        }
        .header { 
            background-color: #007bff; 
            color: white; 
            padding: 20px; 
            text-align: center; 
            margin-bottom: 20px;
            border-radius: 5px;
        }
        .summary { 
            background-color: white; 
            padding: 15px; 
            margin-bottom: 20px; 
            border-radius: 5px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.12);
        }
        .test-results {
            background-color: white;
            padding: 15px;
            border-radius: 5px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.12);
        }
        .test-case {
            border: 1px solid #eee;
            border-radius: 5px;
            padding: 15px;
            margin-bottom: 10px;
        }
        .test-case.passed {
            border-left: 5px solid #28a745;
        }
        .test-case.failed {
            border-left: 5px solid #dc3545;
        }
        .test-details {
            background-color: #f8f9fa;
            padding: 10px;
            border-radius: 5px;
            margin-top: 10px;
            font-family: monospace;
            white-space: pre-wrap;
        }
        .screenshots {
            margin-top: 15px;
        }
        .screenshot img {
            max-width: 300px;
            border: 1px solid #ddd;
            border-radius: 5px;
            margin: 5px;
        }
        h1, h2, h3 {
            color: #333;
        }
        .badge {
            display: inline-block;
            padding: 0.25em 0.6em;
            font-size: 75%;
            font-weight: 700;
            line-height: 1;
            text-align: center;
            white-space: nowrap;
            vertical-align: baseline;
            border-radius: 0.25rem;
            margin-left: 5px;
        }
        .badge.success {
            background-color: #28a745;
            color: white;
        }
        .badge.danger {
            background-color: #dc3545;
            color: white;
        }
        .badge.warning {
            background-color: #ffc107;
            color: black;
        }
        .badge.info {
            background-color: #17a2b8;
            color: white;
        }
        .execution-info {
            margin-top: 10px;
            padding: 10px;
            background-color: #f0f0f0;
            border-left: 5px solid #17a2b8;
            border-radius: 5px;
        }
        .screenshot {
            margin: 10px 0;
        }
        .screenshot img {
            max-width: 100%;
            max-height: 300px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Sahibinden Test Raporu</h1>
        <p>$TIMESTAMP</p>
    </div>
    
    <div class="summary">
        <h2>Test Özeti</h2>
EOF

# Paralel çalıştırma bilgisi ekle
THREAD_COUNT=$(grep -o 'threadCount="[0-9]*"' "$SUREFIRE_DIR"/surefire-reports/TEST-*.xml 2>/dev/null | head -1 | grep -o '[0-9]*')
if [ -z "$THREAD_COUNT" ]; then THREAD_COUNT=1; fi

GRID_USED=$(grep -c "selenium.grid.url" "$SUREFIRE_DIR"/surefire-reports/TEST-*.xml 2>/dev/null || echo "0")
if [ "$GRID_USED" -gt "0" ]; then GRID_STATUS="Aktif"; else GRID_STATUS="Pasif"; fi

cat >> "$REPORT_PATH" << EOF
        <div class="execution-info">
            <p><strong>Test Çalıştırma Bilgisi:</strong></p>
            <p><strong>Paralel Thread Sayısı:</strong> <span class="badge info">$THREAD_COUNT</span></p>
            <p><strong>Selenium Grid:</strong> <span class="badge info">$GRID_STATUS</span></p>
            <p><strong>Tarih Saat:</strong> $TIMESTAMP</p>
        </div>
EOF

# Test sonuçlarını topla
if [ -d "$SUREFIRE_DIR" ]; then
    total_tests=0
    passed_tests=0
    failed_tests=0
    
    echo -e "${GREEN}Surefire rapor dizini bulundu: ${YELLOW}$SUREFIRE_DIR${NC}"
    
    # Debug: Surefire dizinindeki dosyaları listele
    echo -e "${YELLOW}Surefire dizinindeki dosyalar:${NC}"
    ls -la "$SUREFIRE_DIR"
    
    # Test sonuçlarını XML dosyalarından çıkart
    for xml_file in "$SUREFIRE_DIR"/TEST-*.xml; do
        if [ -f "$xml_file" ]; then
            test_class=$(basename "$xml_file" | sed 's/TEST-//' | sed 's/\.xml//')
            echo -e "${GREEN}Test sınıfı XML dosyası işleniyor: ${YELLOW}$test_class${NC}"
            
            # Debug: XML içeriğini kontrol et
            echo -e "${YELLOW}XML içeriği (ilk 5 satır):${NC}"
            head -n 5 "$xml_file"
            
            # XML'den sonuçları çıkar (daha güvenilir yöntem)
            tests=$(grep -o 'tests="[0-9]*"' "$xml_file" | grep -o '[0-9]*')
            failures=$(grep -o 'failures="[0-9]*"' "$xml_file" | grep -o '[0-9]*')
            errors=$(grep -o 'errors="[0-9]*"' "$xml_file" | grep -o '[0-9]*')
            
            echo -e "${GREEN}Test sayısı: ${YELLOW}$tests${NC}, ${GREEN}Failures: ${YELLOW}$failures${NC}, ${GREEN}Errors: ${YELLOW}$errors${NC}"
            
            if [ -z "$tests" ]; then tests=0; fi
            if [ -z "$failures" ]; then failures=0; fi
            if [ -z "$errors" ]; then errors=0; fi
            
            total_tests=$((total_tests + tests))
            
            if [ "$failures" -eq 0 ] && [ "$errors" -eq 0 ]; then
                passed_tests=$((passed_tests + tests))
            else
                failed_tests=$((failed_tests + failures + errors))
            fi
        fi
    done
    
    # Test özetini ekle
    cat >> "$REPORT_PATH" << EOF
        <div>
            <p><strong>Toplam Test:</strong> $total_tests</p>
            <p><strong>Başarılı:</strong> <span class="badge success">$passed_tests</span></p>
            <p><strong>Başarısız:</strong> <span class="badge danger">$failed_tests</span></p>
        </div>
    </div>
    
    <div class="test-results">
        <h2>Test Sonuçları</h2>
EOF
    
    # Her test sınıfı için sonuçları ekle
    for xml_file in "$SUREFIRE_DIR"/TEST-*.xml; do
        if [ -f "$xml_file" ]; then
            test_class=$(basename "$xml_file" | sed 's/TEST-//' | sed 's/\.xml//')
            
            failures=$(grep -o 'failures="[0-9]*"' "$xml_file" | grep -o '[0-9]*')
            errors=$(grep -o 'errors="[0-9]*"' "$xml_file" | grep -o '[0-9]*')
            
            if [ -z "$failures" ]; then failures=0; fi
            if [ -z "$errors" ]; then errors=0; fi
            
            # Test sınıfını açıkça göster
            echo -e "${GREEN}Test sınıfı rapora ekleniyor: ${YELLOW}$test_class${NC}"
            echo -e "${GREEN}Failures: ${YELLOW}$failures${NC}, ${GREEN}Errors: ${YELLOW}$errors${NC}"
            
            cat >> "$REPORT_PATH" << EOF
        <div class="test-case $([ "$failures" -eq 0 ] && [ "$errors" -eq 0 ] && echo "passed" || echo "failed")">
            <h3>$test_class</h3>
EOF
            
            # Test durumunu ekle
            if [ "$failures" -eq 0 ] && [ "$errors" -eq 0 ]; then
                echo "            <p><strong>Durum:</strong> <span class=\"badge success\">Başarılı ✅</span></p>" >> "$REPORT_PATH"
            else
                echo "            <p><strong>Durum:</strong> <span class=\"badge danger\">Başarısız ❌</span></p>" >> "$REPORT_PATH"
                # Hata sayısını ekle
                echo "            <p><strong>Hatalar:</strong> $failures başarısız, $errors hata</p>" >> "$REPORT_PATH"
                
                # Hata detaylarını ekle (daha kapsamlı)
                echo "            <div class=\"test-details\">" >> "$REPORT_PATH"
                
                # Test metodlarını bul
                testmethods=$(grep "<testcase " "$xml_file" | sed -E 's/.*name="([^"]*).*/\1/g')
                echo "<p><strong>Test Metodları:</strong> $testmethods</p>" >> "$REPORT_PATH"
                
                # Başarısız testlerin adlarını çıkar
                failedTests=$(grep -B 1 "<failure" "$xml_file" | grep "<testcase" | sed -E 's/.*name="([^"]*).*/\1/g')
                if [ -n "$failedTests" ]; then
                    echo "<p><strong>Başarısız Testler:</strong> $failedTests</p>" >> "$REPORT_PATH"
                fi
                
                # XML'den hata mesajlarını doğrudan çıkar
                echo "<pre>" >> "$REPORT_PATH"
                # failure ve error içeren context ile satırları çıkar
                grep -B 2 -A 5 "<failure\|<error" "$xml_file" >> "$REPORT_PATH" 2>/dev/null
                echo "</pre>" >> "$REPORT_PATH"
                
                echo "            </div>" >> "$REPORT_PATH"
            fi
            
            # Ekran görüntülerini ekle (varsa)
            if [ -d "$SCREENSHOT_DIR" ]; then
                SCREENSHOTS=$(find "$SCREENSHOT_DIR" -name "*${test_class}*.png" 2>/dev/null || find "$SCREENSHOT_DIR" -type f -name "*.png" 2>/dev/null)
                
                if [ -n "$SCREENSHOTS" ]; then
                    echo "            <div class=\"screenshots\">" >> "$REPORT_PATH"
                    echo "                <h4>Ekran Görüntüleri</h4>" >> "$REPORT_PATH"
                    
                    for screenshot in $SCREENSHOTS; do
                        screenshot_name=$(basename "$screenshot")
                        screenshot_copy="$REPORT_DIR/$screenshot_name"
                        cp "$screenshot" "$screenshot_copy"
                        
                        echo "                <div class=\"screenshot\">" >> "$REPORT_PATH"
                        echo "                    <p>$screenshot_name</p>" >> "$REPORT_PATH"
                        # HTML'de göreli yol kullan
                        rel_path=$(basename "$screenshot_copy")
                        echo "                    <a href=\"$rel_path\" target=\"_blank\"><img src=\"$rel_path\" alt=\"$screenshot_name\"></a>" >> "$REPORT_PATH"
                        echo "                </div>" >> "$REPORT_PATH"
                    done
                    
                    echo "            </div>" >> "$REPORT_PATH"
                fi
            fi
            
            echo "        </div>" >> "$REPORT_PATH"
        fi
    done
    
else
    # Test sonucu bulunamadıysa
    cat >> "$REPORT_PATH" << EOF
        <div>
            <p>Test sonuçları bulunamadı. Önce testleri çalıştırın.</p>
        </div>
    </div>
    
    <div class="test-results">
        <h2>Test Sonuçları</h2>
        <p>Surefire raporları bulunamadı.</p>
EOF
fi

# HTML'i kapat
cat >> "$REPORT_PATH" << EOF
    </div>
</body>
</html>
EOF

echo -e "${GREEN}Test raporu oluşturuldu: ${YELLOW}$REPORT_PATH${NC}"

# Otomatik olarak raporu tarayıcıda aç
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo -e "${GREEN}Rapor tarayıcıda açılıyor...${NC}"
    open "$REPORT_PATH"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo -e "${GREEN}Rapor tarayıcıda açılıyor...${NC}"
    xdg-open "$REPORT_PATH"
else
    echo -e "${GREEN}Rapor tarayıcıda açılıyor...${NC}"
    start "$REPORT_PATH"
fi
echo -e "${GREEN}Rapor tarayıcıda açıldı.${NC}"

echo -e "${YELLOW}------------------------------------${NC}"
echo -e "${GREEN}İşlem tamamlandı.${NC}" 