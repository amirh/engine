// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "flutter/shell/platform/darwin/ios/ios_surface_gl.h"

#include "flutter/fml/trace_event.h"
#include "flutter/shell/gpu/gpu_surface_gl.h"

namespace shell {

IOSSurfaceGL::IOSSurfaceGL(fml::scoped_nsobject<CAEAGLLayer> layer,
                           UIView* root_view,
                           ::shell::GetPlatformViewsController get_platform_views_controller)
    : context_(std::move(layer)), root_view_(root_view), get_platform_views_controller_([get_platform_views_controller retain]) {}

IOSSurfaceGL::~IOSSurfaceGL() = default;

bool IOSSurfaceGL::IsValid() const {
  return context_.IsValid();
}

bool IOSSurfaceGL::ResourceContextMakeCurrent() {
  return IsValid() ? context_.ResourceMakeCurrent() : false;
}

void IOSSurfaceGL::UpdateStorageSizeIfNecessary() {
  if (IsValid()) {
    context_.UpdateStorageSizeIfNecessary();
  }
}

std::unique_ptr<Surface> IOSSurfaceGL::CreateGPUSurface() {
  return std::make_unique<GPUSurfaceGL>(this);
}

intptr_t IOSSurfaceGL::GLContextFBO() const {
  return IsValid() ? context_.framebuffer() : GL_NONE;
}

bool IOSSurfaceGL::UseOffscreenSurface() const {
  // The onscreen surface wraps a GL renderbuffer, which is extremely slow to read.
  // Certain filter effects require making a copy of the current destination, so we
  // always render to an offscreen surface, which will be much quicker to read/copy.
  return true;
}

bool IOSSurfaceGL::GLContextMakeCurrent() {
  return IsValid() ? context_.MakeCurrent() : false;
}

bool IOSSurfaceGL::GLContextClearCurrent() {
  [EAGLContext setCurrentContext:nil];
  return true;
}

bool IOSSurfaceGL::GLContextPresent() {
  TRACE_EVENT0("flutter", "IOSSurfaceGL::GLContextPresent");
  if (!IsValid()) {
    return false;
  }
  if (!context_.PresentRenderBuffer()) {
    return false;
  }
  get_platform_views_controller_.get()()->Present(root_view_);
  return true;
}

flow::ExternalViewEmbedder* IOSSurfaceGL::GetExternalViewEmbedder() {
  return get_platform_views_controller_.get()();
}

}  // namespace shell
