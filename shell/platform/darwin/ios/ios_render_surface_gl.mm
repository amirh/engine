//// Copyright 2017 The Chromium Authors. All rights reserved.
//// Use of this source code is governed by a BSD-style license that can be
//// found in the LICENSE file.
//
//#include "flutter/shell/platform/darwin/ios/ios_render_surface_gl.h"
//
//#include "flutter/fml/trace_event.h"
//#include "flutter/shell/gpu/gpu_surface_gl.h"
//
//namespace shell {
//
//IOSRenderSurfaceGL::IOSRenderSurfaceGL(fml::scoped_nsobject<CAEAGLLayer> layer)
//    : context_(std::move(layer)) {}
//
//IOSRenderSurfaceGL::~IOSRenderSurfaceGL() = default;
//
//bool IOSRenderSurfaceGL::IsValid() const {
//  return context_.IsValid();
//}
//
//bool IOSRenderSurfaceGL::ResourceContextMakeCurrent() {
//  return IsValid() ? context_.ResourceMakeCurrent() : false;
//}
//
//void IOSRenderSurfaceGL::UpdateStorageSizeIfNecessary() {
//  if (IsValid()) {
//    context_.UpdateStorageSizeIfNecessary();
//  }
//}
//
//std::unique_ptr<Surface> IOSRenderSurfaceGL::CreateGPUSurface() {
//  return std::make_unique<GPUSurfaceGL>(this);
//}
//
//intptr_t IOSRenderSurfaceGL::GLContextFBO() const {
//  return IsValid() ? context_.framebuffer() : GL_NONE;
//}
//
//bool IOSRenderSurfaceGL::UseOffscreenSurface() const {
//  // The onscreen surface wraps a GL renderbuffer, which is extremely slow to read.
//  // Certain filter effects require making a copy of the current destination, so we
//  // always render to an offscreen surface, which will be much quicker to read/copy.
//  return true;
//}
//
//bool IOSRenderSurfaceGL::GLContextMakeCurrent() {
//  return IsValid() ? context_.MakeCurrent() : false;
//}
//
//bool IOSRenderSurfaceGL::GLContextClearCurrent() {
//  [EAGLContext setCurrentContext:nil];
//  return true;
//}
//
//bool IOSRenderSurfaceGL::GLContextPresent() {
//  TRACE_EVENT0("flutter", "IOSRenderSurfaceGL::GLContextPresent");
//  if (!IsValid()) {
//    return false;
//  }
//  if (!context_.PresentRenderBuffer()) {
//    return false;
//  }
//  return true;
//}
//
//}  // namespace shell
