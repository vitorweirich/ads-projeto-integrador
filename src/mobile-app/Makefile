# Detecta sistema (Windows vs Unix)
ifeq ($(OS),Windows_NT)
	RM = rmdir /s /q
	RMF = del /f /q
else
	RM = rm -rf
	RMF = rm -f
endif

# Detecta Yarn (fallback para npm)
ifeq ($(OS),Windows_NT)
	YARN := $(shell yarn --version > nul 2>&1 && echo yarn)
else
	YARN := $(shell yarn --version > /dev/null 2>&1 && echo yarn)
endif
NPM := npm

PKG_MANAGER := $(if $(YARN),yarn,$(NPM))

# Diretórios
ANDROID_DIR = android
NODE_MODULES = node_modules

# -------------------------
# Instalação
# -------------------------

install:
	$(PKG_MANAGER) install

reinstall:
	$(RM) $(NODE_MODULES)
	$(PKG_MANAGER) install

# -------------------------
# Limpeza geral
# -------------------------

clean: clean-node clean-android

clean-node:
	$(RM) $(NODE_MODULES)

clean-android:
ifeq ($(OS),Windows_NT)
	cd $(ANDROID_DIR) && gradlew.bat clean
else
	cd $(ANDROID_DIR) && ./gradlew clean
endif

# -------------------------
# Limpeza pesada
# -------------------------

clean-all: clean
	$(RM) $(ANDROID_DIR)/.gradle
	$(RM) $(ANDROID_DIR)/app/build
	$(RM) $(ANDROID_DIR)/build
	$(RM) $(ANDROID_DIR)/.cxx
	$(RM) $(NODE_MODULES)/react-native-reanimated/android/.cxx

# -------------------------
# Reset completo
# -------------------------

reset: clean-all reinstall

# -------------------------
# Rodar app
# -------------------------

build-android:
	node build-android.js


# -------------------------
# Info útil
# -------------------------

which-pm:
	@echo Using $(PKG_MANAGER)