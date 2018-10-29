// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_GL_H_
#define FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_GL_H_

#include "flutter/fml/macros.h"
#include "flutter/fml/platform/darwin/scoped_nsobject.h"
#include "flutter/shell/gpu/gpu_surface_gl.h"
#include "flutter/shell/platform/darwin/ios/ios_gl_context.h"
#include "flutter/shell/platform/darwin/ios/ios_surface.h"

@class CAEAGLLayer;

namespace shell {

class IOSRenderSurfaceGL : public IOSSurface, public GPUSurfaceGLDelegate {
 public:
  IOSRenderSurfaceGL(fml::scoped_nsobject<CAEAGLLayer> layer, EAGLSharegroup* share_group = nullptr);

  ~IOSRenderSurfaceGL() override;

  bool IsValid() const override;

  bool ResourceContextMakeCurrent() override;

  void UpdateStorageSizeIfNecessary() override;

  std::unique_ptr<Surface> CreateGPUSurface() override;

  bool GLContextMakeCurrent() override;

  bool GLContextClearCurrent() override;

  bool GLContextPresent() override;

  intptr_t GLContextFBO() const override;

  bool UseOffscreenSurface() const override;

  // |shell::GPUSurfaceGLDelegate|
  flow::ExternalViewEmbedder* GetExternalViewEmbedder() override;

  EAGLSharegroup* GetShareGroup();
  
 private:
  IOSGLContext context_;

  FML_DISALLOW_COPY_AND_ASSIGN(IOSRenderSurfaceGL);
};

}  // namespace shell

#endif  // FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_GL_H_
