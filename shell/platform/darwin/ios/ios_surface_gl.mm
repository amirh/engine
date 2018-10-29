// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "flutter/shell/platform/darwin/ios/framework/Source/FlutterPlatformViews_Internal.h"

#include "flutter/shell/platform/darwin/ios/ios_surface_gl.h"

#include "flutter/fml/trace_event.h"
#include "flutter/shell/gpu/gpu_surface_gl.h"

namespace shell {

IOSSurfaceGL::IOSSurfaceGL(fml::scoped_nsobject<CAEAGLLayer> layer,
                           UIView* root_view,
                           ::shell::GetPlatformViewsController get_platform_views_controller)
    : render_surface_(layer), root_view_(root_view), get_platform_views_controller_([get_platform_views_controller retain]) {}

IOSSurfaceGL::~IOSSurfaceGL() = default;

bool IOSSurfaceGL::IsValid() const {
  return render_surface_.IsValid();;
}

bool IOSSurfaceGL::ResourceContextMakeCurrent() {
  return render_surface_.ResourceContextMakeCurrent();
}

void IOSSurfaceGL::UpdateStorageSizeIfNecessary() {
  render_surface_.UpdateStorageSizeIfNecessary();
}

std::unique_ptr<Surface> IOSSurfaceGL::CreateGPUSurface() {
  return std::make_unique<GPUSurfaceGL>(this);
}

intptr_t IOSSurfaceGL::GLContextFBO() const {
  return render_surface_.GLContextFBO();
}

bool IOSSurfaceGL::UseOffscreenSurface() const {
  return render_surface_.UseOffscreenSurface();
}

bool IOSSurfaceGL::GLContextMakeCurrent() {
  return render_surface_.GLContextMakeCurrent();
}

bool IOSSurfaceGL::GLContextClearCurrent() {
  return render_surface_.GLContextClearCurrent();
}

bool IOSSurfaceGL::GLContextPresent() {
  if (!render_surface_.GLContextPresent()) {
    return false;
  }

  get_platform_views_controller_.get()()->Present(root_view_);
  return true;
}

flow::ExternalViewEmbedder* IOSSurfaceGL::GetExternalViewEmbedder() {
  return get_platform_views_controller_.get()();
}

EAGLSharegroup* IOSSurfaceGL::GetShareGroup() {
  return render_surface_.GetShareGroup();
}
}  // namespace shell
