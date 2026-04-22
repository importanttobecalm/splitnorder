#!/bin/bash
# 🧪 Local Testing Script — SplitNOrder on M3 8GB Mac
# Kullanım: ./test-local.sh [setup|run|cleanup]

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FLASK_PID=""
TOMCAT_PID=""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ─────────────────────────────────────────────────────────────────────────────

print_header() {
    echo -e "${GREEN}=====================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}=====================================${NC}"
}

print_step() {
    echo -e "${YELLOW}→ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# ─────────────────────────────────────────────────────────────────────────────

setup() {
    print_header "Local Test Setup"

    print_step "Checking Python 3..."
    python3 --version || exit 1
    print_success "Python 3 OK"

    print_step "Checking Java 21..."
    java -version 2>&1 || exit 1
    print_success "Java 21 OK"

    print_step "Building WAR..."
    cd "$PROJECT_ROOT"
    mvn clean package -DskipTests -q
    print_success "WAR built: target/stemsep.war"

    print_step "Checking Flask dependencies..."
    pip3 list | grep -E "Flask|demucs" || {
        print_error "Flask/Demucs not installed. Run: pip3 install -r demucs-server/requirements.txt"
        exit 1
    }
    print_success "Flask dependencies OK"

    print_step "Creating directories..."
    mkdir -p "$PROJECT_ROOT"/{uploads,stems,demucs-output,logs}
    print_success "Directories ready"

    print_header "Setup Complete ✅"
    echo ""
    echo "Next step: ./test-local.sh run"
}

# ─────────────────────────────────────────────────────────────────────────────

run() {
    print_header "Starting Local Services"

    print_step "Starting Flask API (port 5000)..."
    cd "$PROJECT_ROOT/demucs-server"
    python3 app.py > "$PROJECT_ROOT/logs/flask.log" 2>&1 &
    FLASK_PID=$!
    sleep 2
    if kill -0 $FLASK_PID 2>/dev/null; then
        print_success "Flask started (PID: $FLASK_PID)"
    else
        print_error "Flask failed to start"
        cat "$PROJECT_ROOT/logs/flask.log"
        exit 1
    fi

    print_step "Testing Flask health..."
    if curl -s http://localhost:5000/api/health > /dev/null 2>&1; then
        print_success "Flask health check OK"
    else
        print_error "Flask health check failed"
        kill $FLASK_PID 2>/dev/null || true
        exit 1
    fi

    print_step "Starting Tomcat (port 8080)..."
    cd "$PROJECT_ROOT"
    mvn cargo:run > "$PROJECT_ROOT/logs/tomcat.log" 2>&1 &
    TOMCAT_PID=$!
    sleep 5
    if kill -0 $TOMCAT_PID 2>/dev/null; then
        print_success "Tomcat started (PID: $TOMCAT_PID)"
    else
        print_error "Tomcat failed to start"
        cat "$PROJECT_ROOT/logs/tomcat.log" | tail -20
        kill $FLASK_PID 2>/dev/null || true
        exit 1
    fi

    print_step "Testing Tomcat..."
    if curl -s http://localhost:8080/stemsep/ | grep -q "AI StemSep"; then
        print_success "Tomcat running and accessible"
    else
        print_error "Tomcat not responding correctly"
        kill $FLASK_PID $TOMCAT_PID 2>/dev/null || true
        exit 1
    fi

    print_header "All Services Running ✅"
    echo ""
    echo -e "${GREEN}Open these URLs:${NC}"
    echo "  • Home:    ${YELLOW}http://localhost:8080/stemsep/${NC}"
    echo "  • Upload:  ${YELLOW}http://localhost:8080/stemsep/upload${NC}"
    echo "  • History: ${YELLOW}http://localhost:8080/stemsep/history${NC}"
    echo "  • Flask:   ${YELLOW}http://localhost:5000/api/health${NC}"
    echo ""
    echo -e "${GREEN}Log files:${NC}"
    echo "  • Flask:  $PROJECT_ROOT/logs/flask.log"
    echo "  • Tomcat: $PROJECT_ROOT/logs/tomcat.log"
    echo ""
    echo "Press Ctrl+C to stop services..."
    echo ""

    # Keep script running
    trap cleanup EXIT INT TERM
    wait
}

# ─────────────────────────────────────────────────────────────────────────────

cleanup() {
    print_header "Cleaning Up"

    if [ -n "$FLASK_PID" ] && kill -0 $FLASK_PID 2>/dev/null; then
        print_step "Stopping Flask..."
        kill $FLASK_PID 2>/dev/null || true
        sleep 1
        print_success "Flask stopped"
    fi

    if [ -n "$TOMCAT_PID" ] && kill -0 $TOMCAT_PID 2>/dev/null; then
        print_step "Stopping Tomcat..."
        kill $TOMCAT_PID 2>/dev/null || true
        sleep 2
        print_success "Tomcat stopped"
    fi

    print_header "Cleanup Complete ✅"
}

# ─────────────────────────────────────────────────────────────────────────────

main() {
    case "${1:-run}" in
        setup)
            setup
            ;;
        run)
            run
            ;;
        cleanup)
            cleanup
            ;;
        *)
            print_error "Usage: $0 {setup|run|cleanup}"
            exit 1
            ;;
    esac
}

main "$@"
