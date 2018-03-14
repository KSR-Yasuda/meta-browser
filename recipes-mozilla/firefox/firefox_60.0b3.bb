# Copyright (C) 2009-2015, O.S. Systems Software Ltda. All Rights Reserved
# Released under the MIT license (see packages/COPYING)

DESCRIPTION ?= "Browser made by mozilla"
DEPENDS += "curl startup-notification libevent cairo libnotify \
            virtual/libgl pulseaudio yasm-native icu \
            rust-cross-${TARGET_ARCH} cargo-native \
           "
RDEPENDS_${PN}-dev = "dbus"

LICENSE = "MPLv2 | GPLv2+ | LGPLv2.1+"
LIC_FILES_CHKSUM = "file://toolkit/content/license.html;endline=33;md5=f51d0fbc370c551d7371775b4f6544ca"

MOZ_HG_REV="93daac564022a618daa8ae32c422a0ffac8a73d0"

SRC_URI = "https://hg.mozilla.org/releases/mozilla-beta/archive/${MOZ_HG_REV}.tar.bz2;name=archive \
           file://mozconfig \
           file://mozilla-firefox.png \
           file://mozilla-firefox.desktop \
           file://prefs/vendor.js \
           file://prefs/autoconfig.js \
           file://prefs/autoconfig.cfg \
           file://fixes/0001-Enable-to-specify-RUST_TARGET-via-enviroment-variabl.patch \
           file://fixes/0001-Always-accept-the-configure-option-with-gl-provider.patch \
           file://fixes/0001-Fix-a-build-error-of-Gecko-Profiler-for-Linux-ARM.patch \
           file://fixes/0001-Add-a-preference-to-force-enable-touch-events-withou.patch \
           file://fixes/fix-get-cpu-feature-definition-conflict.patch \
           file://gn-configs/ \
           "

SRC_URI[archive.md5sum] = "6b6fc962b4d157f9fd7271364d81dc9e"
SRC_URI[archive.sha256sum] = "0d7a9b1ba4bc7709d05aa7e6c0646e6adc37670cffd13cbb4a9d4f437cf60d53"

PR = "r0"
S = "${WORKDIR}/mozilla-beta-${MOZ_HG_REV}"
MOZ_APP_BASE_VERSION = "${@'${PV}'.replace('esr', '')}"

inherit mozilla rust-common

DISABLE_STATIC=""
EXTRA_OEMAKE += "installdir=${libdir}/${PN}-${MOZ_APP_BASE_VERSION}"

ARM_INSTRUCTION_SET = "arm"

PACKAGECONFIG ??= "${@bb.utils.contains("DISTRO_FEATURES", "alsa", "alsa", "", d)} \
                   ${@bb.utils.contains("DISTRO_FEATURES", "wayland", "wayland", "", d)} \
"
PACKAGECONFIG[alsa] = "--enable-alsa,--disable-alsa,alsa-lib"
PACKAGECONFIG[wayland] = "--enable-default-toolkit=cairo-gtk3-wayland,"
PACKAGECONFIG[glx] = ",,,"
PACKAGECONFIG[egl] = "--with-gl-provider=EGL,,virtual/egl,"
PACKAGECONFIG[openmax] = ",,,"
PACKAGECONFIG[webgl] = ",,,"
PACKAGECONFIG[canvas-gpu] = ",,,"
PACKAGECONFIG[stylo] = "--enable-stylo,--disable-stylo,,"

SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'wayland', \
           ' \
            file://wayland/wayland-hacks.js \
            file://wayland/0001-Permit-to-use-gtk-wayland-3.0-3.18.patch \
            file://wayland/0001-Add-ad-hoc-solution-to-enable-Alt-modifier-on-Waylan.patch \
           ', \
           '', d)}"

# Gecko Embedded's Additional wayland patches to support EGL
#
# TODO: Most of them aren't ported to ESR60 yet
SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'wayland egl', \
           ' \
            file://wayland/egl/0001-GLLibraryEGL-Use-wl_display-to-get-EGLDisplay-on-Way.patch \
            file://wayland/egl/0002-Disable-query-EGL_EXTENSIONS.patch \
            file://wayland/egl/0003-Use-wl_egl_window-as-a-native-EGL-window-on-Wayland.patch \
            file://wayland/egl/0004-Repaint-on-resize-asynchronously.patch \
            file://wayland/egl/0001-GLLibraryLoader-Use-given-symbol-lookup-function-fir.patch \
            file://wayland/egl/0001-Enable-sharing-SharedSurface_EGLImage.patch \
            file://wayland/egl/0001-Call-fEGLImageTargetTexture2D-eariler.patch \
            file://wayland/egl/0001-Create-workaround-to-use-BasicCompositor-to-prevent-.patch \
           ', \
           '', d)}"

# Add a config file to enable GPU acceleration by default.
SRC_URI += "${@bb.utils.contains_any('PACKAGECONFIG', 'glx egl', \
           'file://prefs/gpu.js', '', d)}"

SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'openmax', \
           ' \
            file://openmax/0001-Add-initial-implementation-of-PureOmxPlatformLayer.patch \
            file://openmax/0002-OmxDataDecoder-Fix-a-stall-issue-on-shutting-down.patch \
            file://openmax/openmax.js \
           ', \
           '', d)}"

SRC_URI += "${@bb.utils.contains_any('PACKAGECONFIG', 'webgl', \
           'file://prefs/webgl.js', '', d)}"

SRC_URI += "${@bb.utils.contains_any('PACKAGECONFIG', 'canvas-gpu', \
           'file://prefs/canvas-gpu.js', '', d)}"

python do_check_variables() {
    if bb.utils.contains('PACKAGECONFIG', 'glx egl', True, False, d):
        bb.warn("%s: GLX support will be disabled when EGL is enabled!" % bb.data.getVar('PN', d, 1))
    if bb.utils.contains_any('PACKAGECONFIG', 'glx egl', False, True, d):
        if bb.utils.contains('PACKAGECONFIG', 'webgl', True, False, d):
            bb.warn("%s: WebGL won't be enabled when both glx and egl aren't enabled!" % bb.data.getVar('PN', d, 1))
        if bb.utils.contains('PACKAGECONFIG', 'canvas-gpu', True, False, d):
            bb.warn("%s: Canvas acceleration won't be enabled when both glx and egl aren't enabled!" % bb.data.getVar('PN', d, 1))
}
addtask check_variables before do_configure

def path_ver_cmp(a):
    import os
    import string
    import re

    a               = os.path.abspath(a)
    parse           = string.split(a, "/")
    var             = []
    var_pattern     = re.compile(r"\bv?(\d+(?:.\d+)*)$")
    for seg in reversed(parse):     #Find version no from the bottom of the path
        var_seg     = var_pattern.findall(seg)
        if len(var_seg) > 0:
            var = map(int, var_seg[len(var_seg) - 1].split("."))
            break

    return (var, os.path.dirname(a), os.path.basename(a))

def search_path(pattern):
    import glob

    result  = sorted(glob.glob(pattern), key=path_ver_cmp, reverse=True)

    #result = "" if len(result) == 0 else result[0]
    result = result[0]      #XXX: Raise exception here if no target found

    return result

do_configure() {
    export SHELL=/bin/bash
    export RUST_TARGET_PATH=${STAGING_LIBDIR_NATIVE}/rustlib

    # TODO:
    # Detect include paths more decently.
    # In addition we should fix the build sysetem of firefox instead of
    # setting BINDGEN_CFLAGS.
    #
    INC_CPP='${@search_path("${STAGING_INCDIR}/c++/*")}'
    INC_LLVM='${@search_path("/usr/lib/llvm-*/**/clang/*/include")}'

    export BINDGEN_CFLAGS="${BINDGEN_CFLAGS} \
                           --target=${TARGET_SYS} \
                           -I${INC_CPP} \
                           -I${INC_CPP}/${TARGET_SYS} \
                           -I${INC_LLVM}"

    # TODO:
    # It will be removed later.
    # It should be used only by local.conf or vendor's layer.
    export RUST_TARGET="${RUST_TARGET_SYS}"

    ./mach configure ${CONFIGURE_ARGS}
    cp ${WORKDIR}/gn-configs/*.json ${S}/media/webrtc/gn-configs/
    ./mach build-backend -b GnMozbuildWriter
}

do_compile() {
    export SHELL="/bin/bash"
    export RUST_TARGET_PATH=${STAGING_LIBDIR_NATIVE}/rustlib

    ./mach build
}

do_install() {
    export SHELL="/bin/bash"

    INSTALL_SDK=0 DESTDIR="${D}" ./mach install
}

do_install_append() {
    install -d ${D}${datadir}/applications
    install -d ${D}${datadir}/pixmaps

    install -m 0644 ${WORKDIR}/mozilla-firefox.desktop ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/mozilla-firefox.png ${D}${datadir}/pixmaps/
    install -m 0644 ${WORKDIR}/prefs/vendor.js ${D}${libdir}/${PN}/defaults/pref/
    install -m 0644 ${WORKDIR}/prefs/autoconfig.js ${D}${libdir}/${PN}/defaults/pref/
    install -m 0644 ${WORKDIR}/prefs/autoconfig.cfg ${D}${libdir}/${PN}/
    if [ -n "${@bb.utils.contains_any('PACKAGECONFIG', 'glx egl', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/prefs/gpu.js ${D}${libdir}/${PN}/defaults/pref/
    fi
    if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'openmax', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/openmax/openmax.js ${D}${libdir}/${PN}/defaults/pref/
    fi
    if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'wayland', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/wayland/wayland-hacks.js ${D}${libdir}/${PN}/defaults/pref/
    fi
    if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'webgl', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/prefs/webgl.js ${D}${libdir}/${PN}/defaults/pref/
    fi
    if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'canvas-gpu', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/prefs/canvas-gpu.js ${D}${libdir}/${PN}/defaults/pref/
    fi

    # Fix ownership of files
    chown root:root -R ${D}${datadir}
    chown root:root -R ${D}${libdir}
}

FILES_${PN} = "${bindir}/${PN} \
               ${datadir}/applications/ \
               ${datadir}/pixmaps/ \
               ${libdir}/${PN}/* \
               ${libdir}/${PN}/.autoreg \
               ${bindir}/defaults"
FILES_${PN}-dev += "${datadir}/idl ${bindir}/${PN}-config ${libdir}/${PN}-devel-*"
FILES_${PN}-staticdev += "${libdir}/${PN}-devel-*/sdk/lib/*.a"
FILES_${PN}-dbg += "${libdir}/${PN}/.debug \
                    ${libdir}/${PN}/*/.debug \
                    ${libdir}/${PN}/*/*/.debug \
                    ${libdir}/${PN}/*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/*/.debug \
                    ${bindir}/.debug"

# We don't build XUL as system shared lib, so we can mark all libs as private
PRIVATE_LIBS = " \
    libmozjs.so \
    libxpcom.so \
    libnspr4.so \
    libxul.so \
    libmozalloc.so \
    libplc4.so \
    libplds4.so \
    liblgpllibs.so \
    libmozgtk.so \
    libmozwayland.so \
    libmozsqlite3.so \
    libclearkey.so \
"

# mark libraries also provided by nss as private too
PRIVATE_LIBS += " \
    libfreebl3.so \
    libfreeblpriv3.so \
    libnss3.so \
    libnssckbi.so \
    libsmime3.so \
    libnssutil3.so \
    libnssdbm3.so \
    libssl3.so \
    libsoftokn3.so \
"
