// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_SOFTWARE_H_
#define FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_SOFTWARE_H_

#import <UIKit/UIKit.h>

#include "flutter/fml/macros.h"
#include "flutter/fml/platform/darwin/scoped_nsobject.h"
#include "flutter/shell/gpu/gpu_surface_software.h"
#include "flutter/shell/platform/darwin/ios/ios_surface.h"

@class CALayer;

namespace shell {

class IOSRenderSurfaceSoftware final : public IOSSurface {
 public:
  IOSRenderSurfaceSoftware(fml::scoped_nsobject<CALayer> layer);

  ~IOSRenderSurfaceSoftware() override;

  // |shell::IOSSurface|
  bool IsValid() const override;

  // |shell::IOSSurface|
  bool ResourceContextMakeCurrent() override;

  // |shell::IOSSurface|
  void UpdateStorageSizeIfNecessary() override;

  // |shell::IOSSurface|
  std::unique_ptr<Surface> CreateGPUSurface() override;

  sk_sp<SkSurface> AcquireBackingStore(const SkISize& size);

  bool PresentBackingStore(sk_sp<SkSurface> backing_store);

 private:
  fml::scoped_nsobject<CALayer> layer_;
  sk_sp<SkSurface> sk_surface_;

  FML_DISALLOW_COPY_AND_ASSIGN(IOSRenderSurfaceSoftware);
};

}  // namespace shell

#endif  // FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RENDER_SURFACE_SOFTWARE_H_
