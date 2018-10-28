// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "flutter/shell/platform/darwin/ios/ios_surface_software.h"

#include <QuartzCore/CALayer.h>

#include <memory>

#include "flutter/fml/logging.h"
#include "flutter/fml/platform/darwin/cf_utils.h"
#include "flutter/fml/trace_event.h"
#include "third_party/skia/include/utils/mac/SkCGUtils.h"

namespace shell {

IOSSurfaceSoftware::IOSSurfaceSoftware(fml::scoped_nsobject<CALayer> layer,
                                       UIView* root_view,
                                       ::shell::GetPlatformViewsController get_platform_views_controller)
    : render_surface_(std::move(layer)), root_view_(root_view), get_platform_views_controller_([get_platform_views_controller retain]) { }

IOSSurfaceSoftware::~IOSSurfaceSoftware() = default;

bool IOSSurfaceSoftware::IsValid() const {
  return render_surface_.IsValid();
}

bool IOSSurfaceSoftware::ResourceContextMakeCurrent() {
  return render_surface_.ResourceContextMakeCurrent();
}

void IOSSurfaceSoftware::UpdateStorageSizeIfNecessary() {
  render_surface_.UpdateStorageSizeIfNecessary();
}

std::unique_ptr<Surface> IOSSurfaceSoftware::CreateGPUSurface() {
  if (!IsValid()) {
    return nullptr;
  }

  auto surface = std::make_unique<GPUSurfaceSoftware>(this);

  if (!surface->IsValid()) {
    return nullptr;
  }

  return surface;
}

sk_sp<SkSurface> IOSSurfaceSoftware::AcquireBackingStore(const SkISize& size) {
  return render_surface_.AcquireBackingStore(size);
}

bool IOSSurfaceSoftware::PresentBackingStore(sk_sp<SkSurface> backing_store) {
  if (!render_surface_.PresentBackingStore(backing_store)) {
    return false;
  }

  if (get_platform_views_controller_ != nullptr) {
    get_platform_views_controller_.get()()->Present(root_view_);
  }

  return true;
}

flow::ExternalViewEmbedder* IOSSurfaceSoftware::GetExternalViewEmbedder() {
  return get_platform_views_controller_.get()();
}

}  // namespace shell
