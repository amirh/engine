// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_SURFACE_GL_H_
#define FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_SURFACE_GL_H_

#import "flutter/shell/platform/darwin/ios/framework/Source/FlutterPlatformViews_Internal.h"

#include "flutter/fml/macros.h"
#include "flutter/fml/platform/darwin/scoped_nsobject.h"
#include "flutter/shell/gpu/gpu_surface_gl.h"
#include "flutter/shell/platform/darwin/ios/ios_gl_context.h"
#include "flutter/shell/platform/darwin/ios/ios_surface.h"
#include "flutter/shell/platform/darwin/ios/ios_render_surface_gl.h"

@class CAEAGLLayer;

namespace shell {

class IOSSurfaceGL : public IOSSurface, public GPUSurfaceGLDelegate {
 public:
  IOSSurfaceGL(fml::scoped_nsobject<CAEAGLLayer> layer,
               UIView* root_view,
               ::shell::GetPlatformViewsController get_platform_views_controller);

  ~IOSSurfaceGL() override;

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
  IOSRenderSurfaceGL render_surface_;
  UIView* root_view_;

  fml::scoped_nsprotocol<::shell::GetPlatformViewsController> get_platform_views_controller_;

  FML_DISALLOW_COPY_AND_ASSIGN(IOSSurfaceGL);
};

}  // namespace shell

#endif  // FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_SURFACE_GL_H_
