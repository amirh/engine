// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RESOURCE_CONTEXT_H_
#define FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RESOURCE_CONTEXT_H_

#include <memory>

#include "flutter/flow/texture.h"
#include "flutter/fml/macros.h"
#include "flutter/fml/platform/darwin/scoped_nsobject.h"
#include "flutter/shell/platform/darwin/common/framework/Headers/FlutterTexture.h"
#include "flutter/shell/platform/darwin/ios/rendering_api_selection.h"
#include "third_party/skia/include/gpu/GrContext.h"

namespace flutter {

//------------------------------------------------------------------------------
/// @brief      Manages the lifetime of the on-screen and off-screen rendering
///             contexts on iOS. On-screen contexts are used by Flutter for
///             rendering into the surface. The lifecycle of this context may be
///             tied to the lifecycle of the surface. On the other hand, the
///             lifecycle of the the off-screen context it tied to that of the
///             platform view. This one object used to manage both context
///             because GPU handles may need to be shared between the two
///             context. To achieve this, context may need references to one
///             another at creation time. This one object manages the creation,
///             use and collection of both contexts in a client rendering API
///             agnostic manner.
///
class IOSContext {
 public:
  //----------------------------------------------------------------------------
  /// @brief      Create an iOS context object capable of creating the on-screen
  ///             and off-screen GPU context for use by Skia.
  ///
  ///             In case the engine does not support the specified client
  ///             rendering API, this a `nullptr` may be returned.
  ///
  /// @param[in]  rendering_api  A client rendering API supported by the
  ///                            engine/platform.
  ///
  /// @return     A valid context on success. `nullptr` on failure.
  ///
  static std::unique_ptr<IOSContext> Create(IOSRenderingAPI rendering_api);

  //----------------------------------------------------------------------------
  /// @brief      Collects the context object. This must happen on the thread on
  ///             which this object was created.
  ///
  virtual ~IOSContext();

  //----------------------------------------------------------------------------
  /// @brief      Create a resource context for use on the IO task runner. This
  ///             resource context is used by Skia to upload texture to
  ///             asynchronously and collect resources that are no longer needed
  ///             on the render task runner.
  ///
  /// @attention  Client rendering APIs for which a GrContext cannot be realized
  ///             (software rendering), this method will always return null.
  ///
  /// @return     A non-null Skia context on success. `nullptr` on failure.
  ///
  virtual sk_sp<GrContext> CreateResourceContext() = 0;

  //----------------------------------------------------------------------------
  /// @brief      When using client rendering APIs whose contexts need to be
  ///             bound to a specific thread, the engine will call this method
  ///             to give the on-screen context a chance to bind to the current
  ///             thread.
  ///
  /// @attention  Client rendering APIs that have no-concept of thread local
  ///             bindings (anything that is not OpenGL) will always return
  ///             `true`.
  ///
  /// @attention  Client rendering APIs for which a GrContext cannot be created
  ///             (software rendering) will always return `false`.
  ///
  /// @attention  This binds the on-screen context to the current thread. To
  ///             bind the off-screen context to the thread, use the
  ///             `ResoruceMakeCurrent` method instead.
  ///
  /// @attention  Only one context may be bound to a thread at any given time.
  ///             Making a binding on a thread, clears the old binding.
  ///
  /// @return     If the on-screen context could be bound to the current thread.
  ///
  virtual bool MakeCurrent() = 0;

  //----------------------------------------------------------------------------
  /// @brief      When using client rendering APIs whose contexts need to be
  ///             bound to a specific thread, the engine will call this method
  ///             to give the off-screen context a chance to bind to the current
  ///             thread.
  ///
  /// @attention  Client rendering APIs that have no-concept of thread local
  ///             bindings (anything that is not OpenGL) will always return
  ///             `true`.
  ///
  /// @attention  Client rendering APIs for which a GrContext cannot be created
  ///             (software rendering) will always return `false`.
  ///
  /// @attention  This binds the off-screen context to the current thread. To
  ///             bind the on-screen context to the thread, use the
  ///             `MakeCurrent` method instead.
  ///
  /// @attention  Only one context may be bound to a thread at any given time.
  ///             Making a binding on a thread, clears the old binding.
  ///
  /// @return     If the off-screen context could be bound to the current
  ///             thread.
  ///
  virtual bool ResourceMakeCurrent() = 0;

  //----------------------------------------------------------------------------
  /// @brief      Clears the context binding of the current thread if one is
  ///             present. Does noting otherwise.
  ///
  /// @return     `true` is the current context bound to the thread is cleared.
  ///
  virtual bool ClearCurrent() = 0;

  //----------------------------------------------------------------------------
  /// @brief      Creates an external texture proxy of the appropriate client
  ///             rendering API.
  ///
  /// @param[in]  texture_id  The texture identifier
  /// @param[in]  texture     The texture
  ///
  /// @return     The texture proxy if the rendering backend supports embedder
  ///             provided external textures.
  ///
  virtual std::unique_ptr<Texture> CreateExternalTexture(
      int64_t texture_id,
      fml::scoped_nsobject<NSObject<FlutterTexture>> texture) = 0;

 protected:
  IOSContext();

 private:
  FML_DISALLOW_COPY_AND_ASSIGN(IOSContext);
};

}  // namespace flutter

#endif  // FLUTTER_SHELL_PLATFORM_DARWIN_IOS_IOS_RESOURCE_CONTEXT_H_
