# Copyright (C) 2009-2015, O.S. Systems Software Ltda. All Rights Reserved
# Released under the MIT license (see packages/COPYING)

DESCRIPTION ?= "Browser made by mozilla"
DEPENDS += "alsa-lib curl startup-notification libevent cairo libnotify \
            virtual/libgl pulseaudio yasm-native icu"

LICENSE = "MPLv2 | GPLv2+ | LGPLv2.1+"
LIC_FILES_CHKSUM = "file://toolkit/content/license.html;endline=39;md5=f7e14664a6dca6a06efe93d70f711c0e"

SRC_URI = "https://archive.mozilla.org/pub/firefox/releases/${PV}/source/firefox-${PV}.source.tar.xz;name=archive \
           file://mozilla-firefox.png \
           file://mozilla-firefox.desktop \
           file://vendor.js \
           file://fix-python-path.patch \
           file://remove-needless-windows-dependency.patch \
           file://fix-generate-webidl.patch \
           file://fix-skia-optional-neon.patch \
           file://0041-Fix-a-broken-build-option-with-gl-provider.patch \
           file://0042-Fix-a-build-error-on-enabling-both-Gtk-2-and-EGL.patch \
           file://firefox-50-fix-build-error-without-glx.patch \
           file://mozconfig \
           "

SRC_URI[archive.md5sum] = "41c4fae7ac3a0cf5e0af59edf460a106"
SRC_URI[archive.sha256sum] = "f82151bdae3c12818d3444084e0bbdd3975bf5861beccfcb3afdd37bae9c880b"

PR = "r0"
S = "${WORKDIR}/firefox-${PV}"
#MOZ_APP_BASE_VERSION = "${@'${PV}'.replace('esr', '')}"
MOZ_APP_BASE_VERSION = "52.0"

inherit mozilla

DISABLE_STATIC=""
EXTRA_OEMAKE += "installdir=${libdir}/${PN}-${MOZ_APP_BASE_VERSION}"

ARM_INSTRUCTION_SET = "arm"

PACKAGECONFIG ??= "${@bb.utils.contains("DISTRO_FEATURES", "wayland", "wayland", "", d)}"
PACKAGECONFIG[wayland] = "--enable-default-toolkit=cairo-gtk3-wayland,"
PACKAGECONFIG[glx] = ",,,"
PACKAGECONFIG[egl] = "--with-gl-provider=EGL,,virtual/egl,"
PACKAGECONFIG[openmax] = ",,,"

SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'wayland', \
           'file://wayland-patches/0001-Added-wayland-patch.patch \
            file://wayland-patches/0002-build-fix.patch \
            file://wayland-patches/0003-Debug-version.patch \
            file://wayland-patches/0004-Removed-debug-Dump-code.patch \
            file://wayland-patches/0005-Dynamically-resize-wl_buffer-according-to-attached-w.patch \
            file://wayland-patches/0006-fixed-rendering-via.-frame-callback.patch \
            file://wayland-patches/0007-Create-.mozconfig.patch \
            file://wayland-patches/0008-Fixed-flickering-when-wl_buffer-is-altered.patch \
            file://wayland-patches/0009-Added-wayland-lib-wrapper.patch \
            file://wayland-patches/0010-Fixed-CurrentX11TimeGetter-usage-fixed-WindowSurface.patch \
            file://wayland-patches/0011-Fixed-timestamps.patch \
            file://wayland-patches/0012-Import-updated-mozcontainer.cpp-gfxPlatform.cpp-patc.patch \
            file://wayland-patches/0013-fixed-crash-at-browser-end.patch \
            file://wayland-patches/0014-Removed-wayland-client-from-libxul.so.patch \
            file://wayland-patches/0015-Removed-unused-code.patch \
            file://wayland-patches/0016-Removed-NS_NATIVE_COMPOSITOR_DISPLAY_X11.patch \
            file://wayland-patches/0017-Link-wayland-run-time-and-provide-fallback-library-w.patch \
            file://wayland-patches/0018-Added-clipboard-patch-from-mozbz-1282015.patch \
            file://wayland-patches/0019-WIP-Added-build-config-when-wayland-is-not-enabled-o.patch \
            file://wayland-patches/0020-Added-enable-wayland-configure-option.patch \
            file://wayland-patches/0021-Use-MOZ_WAYLAND-instead-of-GDK_WINDOWING_WAYLAND.patch \
            file://wayland-patches/0022-Don-t-install-libmozwayland-when-wayland-is-disabled.patch \
            file://wayland-patches/0023-Improved-wayland-configure-defines.patch \
            file://wayland-patches/0024-Updated-configure-script-according-to-mozbz-1299083.patch \
            file://wayland-patches/0025-Removed-event-queue-from-mozcontainer.patch \
            file://wayland-patches/0026-WindowSurfaceWayland-refactorization.patch \
            file://wayland-patches/0027-tabs-replacement.patch \
            file://wayland-patches/0028-Optimized-back-buffer-buffer-switches.patch \
            file://wayland-patches/0029-Don-t-read-wayland-events-when-poll-fails.patch \
            file://wayland-patches/0030-Force-release-unused-back-buffers.patch \
            file://wayland-patches/0031-Moved-wayland-loop-to-Compositor-thread.patch \
            file://wayland-patches/0032-Removed-ImageBuffer-and-draw-directly-to-wayland-bac.patch \
            file://wayland-patches/0033-Removed-old-comments.patch \
            file://wayland-patches/0034-Fixed-crash-when-pasted-to-clipboard.patch \
            file://wayland-patches/0035-GLLibraryEGL-Use-wl_display-to-get-EGLDisplay-on-Way.patch \
            file://wayland-patches/0036-Use-wl_egl_window-as-a-native-EGL-window-on-Wayland.patch \
            file://wayland-patches/0037-Disable-query-EGL_EXTENSIONS.patch \
            file://wayland-patches/0038-Wayland-Detect-existence-of-wayland-libraries.patch \
            file://wayland-patches/0039-Wayland-Resize-wl_egl_window-when-the-nsWindow-is-re.patch \
            file://wayland-patches/0040-GLContextPrividerEGL-Remove-needless-code.patch \
            file://wayland-patches/0043-Add-with-gl-provider-EGL-to-.mozconfig.patch \
            file://wayland-patches/0044-Fix-build-error-for-invalid-symbol.patch \
            file://wayland-patches/0001-Permit-to-use-gtk-wayland-3.0-3.18.patch \
            file://wayland-patches/0001-Adopt-wl_data_offer_listener-for-older-Wayland-under.patch \
           ', \
           '', d)}"

# Add a config file to enable GPU acceleration by default.
SRC_URI += "${@bb.utils.contains_any('PACKAGECONFIG', 'glx egl', \
           'file://gpu.js', '', d)}"

# Current EGL patch for Wayland doesn't work well on windowed mode.
# To avoid this issue, force use fullscreen mode by default.
SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'wayland egl', \
           'file://wayland-patches/frameless.patch', '', d)}"

SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', 'openmax', \
           'file://openmax/0001-Add-initial-implementation-of-PureOmxPlatformLayer.patch \
            file://openmax/openmax.js \
           ', \
           '', d)}"

python do_check_variables() {
    if bb.utils.contains('PACKAGECONFIG', 'glx egl', True, False, d):
        bb.warn("%s: GLX support will be disabled when EGL is enabled!" % bb.data.getVar('PN', d, 1))
}
addtask check_variables before do_configure

do_install_append() {
    install -d ${D}${datadir}/applications
    install -d ${D}${datadir}/pixmaps

    install -m 0644 ${WORKDIR}/mozilla-firefox.desktop ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/mozilla-firefox.png ${D}${datadir}/pixmaps/
    install -m 0644 ${WORKDIR}/vendor.js ${D}${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/defaults/pref/
    if [ -n "${@bb.utils.contains_any('PACKAGECONFIG', 'glx egl', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/gpu.js ${D}${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/defaults/pref/
    fi
    if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'openmax', '1', '', d)}" ]; then
        install -m 0644 ${WORKDIR}/openmax/openmax.js ${D}${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/defaults/pref/
    fi

    # Fix ownership of files
    chown root:root -R ${D}${datadir}
    chown root:root -R ${D}${libdir}
}

FILES_${PN} = "${bindir}/${PN} \
               ${datadir}/applications/ \
               ${datadir}/pixmaps/ \
               ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/* \
               ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/.autoreg \
               ${bindir}/defaults"
FILES_${PN}-dev += "${datadir}/idl ${bindir}/${PN}-config ${libdir}/${PN}-devel-*"
FILES_${PN}-staticdev += "${libdir}/${PN}-devel-*/sdk/lib/*.a"
FILES_${PN}-dbg += "${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/*/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/*/.debug \
                    ${bindir}/.debug"

# We don't build XUL as system shared lib, so we can mark all libs as private
PRIVATE_LIBS = "libmozjs.so \
                libxpcom.so \
                libnspr4.so \
                libxul.so \
                libmozalloc.so \
                libplc4.so \
                libplds4.so \
                liblgpllibs.so \
                libmozgtk.so"

# mark libraries also provided by nss as private too
PRIVATE_LIBS += " \
    libfreebl3.so \
    libnss3.so \
    libnssckbi.so \
    libsmime3.so \
    libnssutil3.so \
    libnssdbm3.so \
    libssl3.so \
    libsoftokn3.so \
"